package com.juezlti.repository.storage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juezlti.repository.models.yapexil.ExerciseMetadata;
import com.juezlti.repository.models.yapexil.SolutionMetadata;
import com.juezlti.repository.models.yapexil.StatementMetadata;
import com.juezlti.repository.models.yapexil.TestMetadata;
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
            Files.copy(file.getInputStream(), destiny);
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
}