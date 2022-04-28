package com.juezlti.repository.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.juezlti.repository.models.Exercise;
import com.juezlti.repository.models.yapexil.ExerciseMetadata;
import com.juezlti.repository.models.yapexil.SolutionMetadata;
import com.juezlti.repository.models.yapexil.StatementMetadata;
import com.juezlti.repository.models.yapexil.TestMetadata;
import com.juezlti.repository.repository.ExerciseRepository;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import static com.juezlti.repository.service.ExerciseService.*;

@Service
public class FileService {

    @Value("${files-storage.basepath:/codetest}")
    private String baseUploadStrPath;

    @Value("${files-storage.upload:/upload}")
    private String uploadStrPath;

    @Value("${files-storage.exercises:/exercises}")
    private String exercisesStrPath;
    
    @Autowired
    private ExerciseRepository exerciseRepository;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(baseUploadStrPath));
            Files.createDirectories(Paths.get(baseUploadStrPath, uploadStrPath));
            Files.createDirectories(Paths.get(baseUploadStrPath, exercisesStrPath));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    public String getBaseUploadStrPath() {
        return baseUploadStrPath;
    }

    public String getUploadStrPath() {
        return uploadStrPath;
    }

    public String getExercisesStrPath() {
        return exercisesStrPath;
    }

    public Path save(MultipartFile file, String... strPath) {
        try {
            Path root = Optional.ofNullable(strPath.length == 0 ? null : strPath[0])
                            .map(argPath -> Paths.get(baseUploadStrPath, argPath))
                            .orElse(Paths.get(baseUploadStrPath));
            
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }

            Path destiny = root.resolve(file.getOriginalFilename());
            CopyOption[] options = { StandardCopyOption.REPLACE_EXISTING };
            Files.copy(file.getInputStream(), destiny, options);

            return destiny;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public Resource load(String filename) {
        try {
            Path file = Paths.get(baseUploadStrPath, uploadStrPath)
                    .resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public Resource loadExerciseStatement(String id, String filename) {
        return getResource(id, filename, STATEMENTS_FOLDER);
    }
    public Resource loadExerciseTests(String id, String filename) {
        return getResource(id, filename, TESTS_FOLDER);
    }
    public Resource loadExerciseSolutions(String id, String filename) {
        return getResource(id, filename, SOLUTIONS_FOLDER);
    }

    public List<Path> getExerciseMetadataFiles(String id, boolean onlyFirstLevel) {
        Path exFolderPath = Paths.get(baseUploadStrPath, exercisesStrPath, id);
        int maxDepth = onlyFirstLevel ? 1 : 3;
        try {
            return Files
                    .walk(exFolderPath, maxDepth)
                    .filter(el -> !el.toFile().isDirectory() && "metadata.json".equals(el.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ExerciseMetadata getExerciseMetadata(String id){
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Optional<Path> path = getExerciseMetadataFiles(id, true).stream().findFirst();
        try {
            return objectMapper.readValue(
                    readFileContentAsString(path.get()),
                    new TypeReference<ExerciseMetadata>() {}
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<StatementMetadata> getExerciseStatementsMetadata(String exId) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return getExerciseMetadataFiles(exId, false)
                .stream()
                .filter(el -> STATEMENTS_FOLDER.equals(el.getParent().getParent().getFileName().toString()))
                .map(el -> {
                            try {
                                
                                StatementMetadata aux = objectMapper.readValue(
                                        readFileContentAsString(el),
                                        new TypeReference<StatementMetadata>() {}
                                );
                                aux.setExerciseId(exId);
                                return aux;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                )
                .collect(Collectors.toList());
    }

    public List<TestMetadata> getExerciseTestMetadata(String exId){
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String base = Paths.get(baseUploadStrPath, exercisesStrPath).toString();

        
        return getExerciseMetadataFiles(exId, false)
                .stream()
                .filter(el -> TESTS_FOLDER.equals(el.getParent().getParent().getFileName().toString()))
                .map(el -> {
                            try {
                                TestMetadata aux = objectMapper.readValue(
                                        readFileContentAsString(el),
                                        new TypeReference<TestMetadata>() {}
                                );
                                aux.setExerciseId(exId);                               
                                aux.setInputValue(
                                        readFileContentAsString(
                                                Paths.get(aux.calcInputValue(base))
                                        )
                                );                              
                                aux.setOutputValue(
                                        readFileContentAsString(
                                                Paths.get(aux.calcOutputValue(base))
                                        )
                                );                               
                                return aux;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                )
                .collect(Collectors.toList());
    }

    public List<SolutionMetadata> getExerciseSolutionsMetadata(String exId) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return getExerciseMetadataFiles(exId, false)
                .stream()
                .filter(el -> SOLUTIONS_FOLDER.equals(el.getParent().getParent().getFileName().toString()))
                .map(el -> {
                            try {
                                SolutionMetadata aux = objectMapper.readValue(
                                        readFileContentAsString(el),
                                        new TypeReference<SolutionMetadata>() {}
                                );
                                aux.setExerciseId(exId);
                                return aux;
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                )
                .collect(Collectors.toList());
    }

    private Resource getResource(String id, String filename, String folder) {
        try {
            Path file = Paths.get(baseUploadStrPath, exercisesStrPath, id, folder)
                    .resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public String readFileContentAsString(Path filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8))
        {   stream.forEach(s -> contentBuilder.append(s).append("\n")); }
		catch (IOException e)
        {   e.printStackTrace();    }
        return contentBuilder.toString();
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(Paths.get(baseUploadStrPath)
                .toFile());
    }

    public List<Path> loadAll() {
        try {
            Path root = Paths.get(baseUploadStrPath, uploadStrPath);
            if (Files.exists(root)) {
                return Files.walk(root, 1)
                        .filter(path -> !path.equals(root) && !Files.isDirectory(path))
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (IOException e) {
            throw new RuntimeException("Could not list the files!");
        }
    }

    public void addFolder(String route, String folder, ZipOutputStream zip)throws Exception{

        File directory = new File(folder);        
        for( String filename : directory.list()){
            int vueltas = 0;
            if(route.equals("")){
                addFile(directory.getName(),folder + "/" + filename,zip,vueltas);
                vueltas++;
            }else{
                addFile(route + "/" + directory.getName(),folder + "/" + filename,zip,vueltas);
            }
        }

    }

    public void addFile(String route, String directory, ZipOutputStream zip,int vueltas)throws Exception{
        File file = new File(directory);
        if(file.isDirectory()){
            this.addFolder(route, directory, zip);
        }else{
            
            byte[] buffer = new byte[1024];
            FileInputStream inputStream = new FileInputStream(file);
            int reader;
            zip.putNextEntry(new ZipEntry("/" + file.getName()));
            while(0 < (reader = inputStream.read(buffer))){

                zip.write(buffer,0,reader);

                }   
            }
    }
    
   
    public void compress(String file, String zipFile)throws Exception{

        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
        addFolder("", file, zip);
        zip.flush();
        zip.close();
    }

    public JSONObject generateMetadatas(Exercise receivedExercise) throws Exception{

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		LocalDateTime actualDate = LocalDateTime.now();
		JSONObject jsonResult = null;
		receivedExercise.setCreated_at(actualDate);								
		Exercise createdExercise = exerciseRepository.save(receivedExercise);
		createdExercise.setAkId(UUID.randomUUID().toString());
		exerciseRepository.save(createdExercise);

		String exerciseDirectory = this.getBaseUploadStrPath() +
		this.getExercisesStrPath() +
		"/" +
		createdExercise.getAkId();

		String uploadDirectory = this.getBaseUploadStrPath() +
		this.getUploadStrPath() +
		"/" +
		createdExercise.getAkId();

		

		Path exerciseMainPath = Paths.get(exerciseDirectory);

		if (!Files.exists(exerciseMainPath)) {

			Files.createDirectory(exerciseMainPath);
			File file = new File(exerciseDirectory + File.separator + "metadata.json");

			if (!file.getParentFile().exists()) {

				file.getParentFile().mkdirs();

			}
			if (file.exists ()) {

				file.delete();

			}

			file.createNewFile();
			ExerciseMetadata exerciseMetadata = new ExerciseMetadata(createdExercise);
			SolutionMetadata solutionMetadata = new SolutionMetadata(createdExercise);
			StatementMetadata statementMetadata = new StatementMetadata(createdExercise);
			TestMetadata testMetadata = new TestMetadata(createdExercise);

			String jsonObject = gson.toJson(exerciseMetadata);
			BufferedWriter br = new BufferedWriter(new FileWriter(file));
			br.write(jsonObject);
			br.flush();
			br.close();
			
			String fileTestDirectory = exerciseDirectory + "/" + TESTS_FOLDER;
			String fileSolutionsDirectory = exerciseDirectory + "/" + SOLUTIONS_FOLDER;
			String fileStatementsDirectory = exerciseDirectory + "/" + STATEMENTS_FOLDER;
			Files.createDirectories(Paths.get(fileTestDirectory));
			Files.createDirectories(Paths.get(fileSolutionsDirectory));
			Files.createDirectories(Paths.get(fileStatementsDirectory));

			////TEST
			File testFile = new File(fileTestDirectory + "/" + testMetadata.getId() + "/" +"metadata.json");

			if (! testFile.getParentFile ().exists()) {

				testFile.getParentFile().mkdirs();

			}
			if (!testFile.exists ()) {

				testFile.createNewFile();

			}
												
			jsonObject = gson.toJson(testMetadata);
			br = new BufferedWriter(new FileWriter(testFile));
			br.write(jsonObject);
			br.flush();
			br.close();
			
			String inputTestDirectory = fileTestDirectory + "/" + testMetadata.getId() + "/input.txt";
			String outputTestDirectory = fileTestDirectory + "/" + testMetadata.getId() + "/output.txt";

			br = new BufferedWriter(new FileWriter(inputTestDirectory));
			br.write(createdExercise.getExercise_input_test());
			br.flush();
			br.close();

			br = new BufferedWriter(new FileWriter(outputTestDirectory));
			br.write(createdExercise.getExercise_output_test());
			br.flush();
			br.close();
			////TEST

			////STATEMENT
			File statementFile = new File(fileStatementsDirectory + "/" + statementMetadata.getId() + "/" +"metadata.json");

			if (! statementFile.getParentFile().exists()) {

				statementFile.getParentFile().mkdirs();

			}
			if (!statementFile.exists()) {

				statementFile.createNewFile();

			}
												
			jsonObject = gson.toJson(statementMetadata);
			br = new BufferedWriter(new FileWriter(statementFile));
			br.write(jsonObject);
			br.flush();
			br.close();
			
			File statementLabel = new File(fileStatementsDirectory + "/" + statementMetadata.getStatementStringPath());					
			if (!statementLabel.exists()) {

				statementLabel.createNewFile();	

			}

			br = new BufferedWriter(new FileWriter(statementLabel));
			br.write(createdExercise.getStatement());
			br.flush();
			br.close();
			// STATEMENT

			// SOLUTION
			File solutionFile = new File(fileSolutionsDirectory + "/" + solutionMetadata.getId() + "/" +"metadata.json");

			if (! solutionFile.getParentFile ().exists()) {

				solutionFile.getParentFile().mkdirs();

			}
			if (!solutionFile.exists ()) {

				solutionFile.createNewFile();

			}
												
			jsonObject = gson.toJson(solutionMetadata);
			br = new BufferedWriter(new FileWriter(solutionFile));
			br.write(jsonObject);
			br.flush();
			br.close();
			
			File solutionLabel = new File(fileSolutionsDirectory + "/" + solutionMetadata.getSolutionStringPath());
			
			if (!solutionLabel.exists ()) {

				solutionLabel.createNewFile();

			}
						
			br = new BufferedWriter(new FileWriter(solutionLabel));
			br.write(createdExercise.getExercise_solution());
			br.flush();
			br.close();	
			// SOLUTION
			
			//ZIP
			String zipDestiny = uploadDirectory + ".zip";
			String directoryToZip = exerciseDirectory;
		      
			zipFolder(new File(directoryToZip),
                new File(zipDestiny));
			//ZIP

			jsonResult = new JSONObject(createdExercise);

			return jsonResult;

		} else {
			
		System.out.println("Directory already exists");
			return null;
		}
	}

	public void zipFolder(File srcFolder, File destZipFile) throws Exception {
        try (FileOutputStream fileWriter = new FileOutputStream(destZipFile);
                ZipOutputStream zip = new ZipOutputStream(fileWriter)) {

            addFolderToZip(srcFolder, srcFolder, zip);
        }
    }

    private void addFileToZip(File rootPath, File srcFile, ZipOutputStream zip) throws Exception {

        if (srcFile.isDirectory()) {
            addFolderToZip(rootPath, srcFile, zip);
        } else {
            byte[] buf = new byte[1024];
            int len;
            try (FileInputStream in = new FileInputStream(srcFile)) {
                String name = srcFile.getPath();
                name = name.replace(rootPath.getPath(), "");
				name = name.substring(1);
                System.out.println("Zip " + srcFile + "\n to " + name);
                zip.putNextEntry(new ZipEntry(name));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            }
        }
    }

    private void addFolderToZip(File rootPath, File srcFolder, ZipOutputStream zip) throws Exception {
        for (File fileName : srcFolder.listFiles()) {
            addFileToZip(rootPath, fileName, zip);
        }
    }

}
