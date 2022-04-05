package com.juezlti.repository.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.juezlti.repository.models.yapexil.ExerciseMetadata;
import com.juezlti.repository.models.yapexil.SolutionMetadata;
import com.juezlti.repository.models.yapexil.StatementMetadata;
import com.juezlti.repository.models.yapexil.TestMetadata;
import com.juezlti.repository.storage.FileService;
import com.juezlti.repository.util.HtmlFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lingala.zip4j.ZipFile;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juezlti.repository.models.Exercise;
import com.juezlti.repository.repository.ExerciseRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import com.juezlti.repository.service.ExerciseService;
import org.springframework.web.servlet.HandlerMapping;

import static com.juezlti.repository.service.ExerciseService.STATEMENTS_FOLDER;
import static com.juezlti.repository.service.ExerciseService.SOLUTIONS_FOLDER;
import static com.juezlti.repository.service.ExerciseService.TESTS_FOLDER;

import com.google.gson.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("/api/exercises")
public class ExerciseController {

	@Value("${files-storage.upload:/upload}")
	private String uploadPath;
	
	@Value("${files-storage.exercises:/exercises}")
	private String exercisesPath;
	
	@Autowired
    private ExerciseRepository exerciseRepository;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	private HtmlFilter htmlFilterFactory;
	
	@Autowired
	private ExerciseService exerciseService;
	
	public enum ExerciseData {
		STATEMENT,
		TEST,
		SOLUTION
	}

	@PostMapping(path = "/createExercise")
	public String createExercises(@RequestBody String exerciseJson) {

		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		List<Exercise> exercises = new ArrayList<>();
		Exercise createdExercise;
		String jsonResponse="";
		String jsonObject = null;
		JSONObject jsonResult = null;

		try {
			exercises = objectMapper.readValue(exerciseJson, new TypeReference<List<Exercise>>() {
			});
			for (Exercise receivedExercise : exercises) {
				try {
					
					GsonBuilder builder = new GsonBuilder();
					Gson gson = builder.create();
					Date actualDate = new Date();

					receivedExercise.setCreated_at(actualDate);					
					createdExercise = exerciseRepository.save(receivedExercise);
					createdExercise.setAkId(createdExercise.getId());
					exerciseRepository.save(createdExercise);
									
					String exerciseDirectory = fileService.getBaseUploadStrPath() +
					fileService.getExercisesStrPath() +
					"/" +
					createdExercise.getId();

					String uploadDirectory = fileService.getBaseUploadStrPath() +
					fileService.getUploadStrPath() +
					"/" +
					createdExercise.getId();

					Path exerciseMainPath = Paths.get(exerciseDirectory);

					if (!Files.exists(exerciseMainPath)) {

						Files.createDirectory(exerciseMainPath);
						File file = new File(exerciseDirectory + File.separator + "metadata.json");

						if (! file.getParentFile ().exists()) {

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

						jsonObject = gson.toJson(exerciseMetadata);
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

						////STATEMETN
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
						String uploadDestiny = uploadDirectory + ".zip";
						fileService.compress(exerciseDirectory, uploadDestiny);
						//ZIP

						jsonResult = new JSONObject(createdExercise);

					} else {
						
					System.out.println("Directory already exists");

					}

				} catch (Exception ex) {
					log.error("Unexpected error trying to create exercise {}", ex);
					return new String("Unexpected error trying to create exercise " + HttpStatus.BAD_REQUEST);
				}
			}

			jsonResponse = jsonResult.toString();

		} catch (JsonProcessingException e) {
			log.warn("Failure processing JSON", e);
			return new String("Failure processing JSON " + HttpStatus.BAD_REQUEST);
		}
		return jsonResponse;
	}
	
	@PostMapping("import-file")
	public ResponseEntity<String> uploadFile(
			@RequestParam("exercise") MultipartFile file,
			@RequestParam("PHPSESSID") String phpSessionId
	) {
		try {
			if(file == null || file.getOriginalFilename() == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.build();
			}
			
			Path savedPath = fileService.save(file, uploadPath);
			String akId = file.getOriginalFilename().replace(".zip", "");

			ZipFile zipFile = new ZipFile(savedPath.toFile());
			zipFile.extractAll(
				fileService.getBaseUploadStrPath() +
							exercisesPath +
							"/" +
							akId
			);

			Optional<Exercise> repositoryExercise = Optional.ofNullable(exerciseRepository.findByAkId(akId));

			if(!repositoryExercise.isPresent()){

				Exercise akExercise = new Exercise();
				akExercise.setAkId(akId);
				ExerciseMetadata exMetadata = fileService.getExerciseMetadata(akId);
				List<StatementMetadata> statementsMetadata = fileService.getExerciseStatementsMetadata(akId);
				List<SolutionMetadata> solutionsMetadata = fileService.getExerciseSolutionsMetadata(akId);

				SolutionMetadata firstSolution = solutionsMetadata
						.stream()
						.findFirst()
						.get();
			
				StatementMetadata firstStatement = statementsMetadata
									.stream()
									.filter(el -> "en".equals(el.getNat_lang()))
									.findFirst()
									.get();

				akExercise.setTitle(exMetadata.getTitle());
				switch (firstStatement.getFormat().toLowerCase()){
					case "txt" :
					case "html":
						Path statementPathTxt = Paths.get(
								fileService.getBaseUploadStrPath(),
								"exercises",
								firstStatement.getFileStringPath()
						).toAbsolutePath();

						String statementContentTxt = fileService.readFileContentAsString(statementPathTxt);
						akExercise.setStatement(htmlFilterFactory.policyFactory().sanitize(statementContentTxt));
						break;
					case "pdf":
					default:
						akExercise.setStatement("PDF");
						break;
				}
				akExercise.setDifficulty(
						capitalize(exMetadata.getDifficulty().toLowerCase())
				);

			 	akExercise.setExercise_language(
			 			firstSolution.getLang().toLowerCase()
			 );

			Exercise savedExercise = exerciseRepository.save(akExercise);
			
			return ResponseEntity.status(HttpStatus.OK)
					.body(savedExercise.getId());
			}else{
				return ResponseEntity.status(HttpStatus.OK)
						.body((repositoryExercise.get()).getId());
			}
						
		} catch (Exception e) {
			System.out.println("FAILED");
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(e.getMessage());
		}
	}

	@Data
	@AllArgsConstructor
	public class ExerciseItem {
		String id;
		List<String> statementsUrl;
		List<String> testsUrl;
		List<String> solutionsUrl;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public class ImportExerciseRequestStruct {
		@JsonProperty("PHPSESSID")
		String sessionId;

		@JsonProperty("exercise[]")
		String exerciseId;
	}

	@GetMapping("{id}/export")
	public ResponseEntity<Resource> exportExerciseZip(
			@PathVariable String id, HttpServletRequest request
	){
		Resource file = fileService.load(id+".zip");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}

	@GetMapping("{id}/statements")
	public ResponseEntity<List<StatementMetadata>> getExerciseStatements(
			@PathVariable String id, HttpServletRequest request
	){
		return ResponseEntity.ok()
				.body(fileService.getExerciseStatementsMetadata(id));
	}

	@GetMapping("{id}/tests")
	public ResponseEntity<List<TestMetadata>> getExerciseTests(
			@PathVariable String id, HttpServletRequest request
	){
		return ResponseEntity.ok()
				.body(fileService.getExerciseTestMetadata(id));
	}

	@GetMapping("external/{id}")
	public ResponseEntity<ExerciseItem> getAuthorkitExercise(@PathVariable String id){
		ExerciseItem aux = new ExerciseItem(id, null, null, null);
		aux.setStatementsUrl(exerciseService.getExerciseStatements(id));
		aux.setSolutionsUrl(exerciseService.getExerciseSolutions(id));
		aux.setTestsUrl(exerciseService.getExerciseTests(id));

		return ResponseEntity.ok().body(aux);
	}

	@GetMapping("external/{id}/statement/**")
	public ResponseEntity<Resource> getAuthorkitExerciseStatement(
			@PathVariable String id, HttpServletRequest request
	){
		return getExerciseData(id, ExerciseData.STATEMENT, request);
	}

	@GetMapping("external/{id}/test/**")
	public ResponseEntity<Resource> getAuthorkitExerciseTests(
			@PathVariable String id, HttpServletRequest request
	){
		return getExerciseData(id, ExerciseData.TEST, request);
	}

	@GetMapping("external/{id}/solution/**")
	public ResponseEntity<Resource> getAuthorkitExerciseSolution(
			@PathVariable String id, HttpServletRequest request
	){
		return getExerciseData(id, ExerciseData.SOLUTION, request);
	}

	private ResponseEntity<Resource> getExerciseData(String id, ExerciseData exData, HttpServletRequest request) {
		String filenameParsed = extractPath(request, true);
		String pathParsed = extractPath(request, false);
		Resource fileResource = null;
		switch (exData){
			case STATEMENT:
			default:
				fileResource = fileService.loadExerciseStatement(id, pathParsed);
				break;
			case TEST:
				fileResource = fileService.loadExerciseTests(id, pathParsed);
				break;
			case SOLUTION:
				fileResource = fileService.loadExerciseSolutions(id, pathParsed);
				break;
		}

		return ResponseEntity
				.ok()
				.headers(
						buildHttpHeaders(filenameParsed, fileResource)
				)
				.body(fileResource);
	}

	private HttpHeaders buildHttpHeaders(String pathParsed, Resource fileResource) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(
				getMimeType(fileResource)
		);
		responseHeaders.setContentDisposition(
				ContentDisposition.parse("attachment; filename=\"" + pathParsed + "\"")
		);
		return responseHeaders;
	}

	private String extractPath(HttpServletRequest request, boolean decode) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String matchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		String extractedPath = new AntPathMatcher().extractPathWithinPattern(matchPattern, path);
		if(!decode){
			return extractedPath;
		}

		String filenameParsed = null;
		try {
			filenameParsed = URLDecoder.decode(
					extractedPath.split("/")[1],
					StandardCharsets.UTF_8.toString()
			);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return filenameParsed;
	}
	
	public MediaType getMimeType(Resource resource){
		String mimeType = null;
		try {
			mimeType = Files.probeContentType(resource.getFile().toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return MediaType.valueOf(
				Optional.ofNullable(mimeType).orElse("text/plain")
		);
	}

	@PostMapping(path = "/getAllExercises")
	public List<Exercise> getAllExercises(@RequestParam("exerciseIds") String exerciseIds) {
		List<String> exercisesIdArr = Arrays.asList(exerciseIds.split(","));
		return exerciseRepository.findByIdIn(exercisesIdArr);
	}
	
	@GetMapping(path = "/getAllExercises/{value}")
	public List<List> getAllExercisesPaged(@PathVariable("value") Integer page) {
		
		double total = Math.ceil((double)exerciseRepository.findAllExercises()/10);
		List<List> listas = new ArrayList<List>();
		List<Double> total1 = new ArrayList<Double>();
		total1.add(total);
		List<Exercise> exercises = exerciseRepository.findAllExercises(PageRequest.of(page, 10));
		listas.add(exercises);
		listas.add(total1);

		return listas;
	}
	
	@GetMapping(path = "/getKeywords/{value}")
	public List<Exercise> getKeywords(@PathVariable("value") String keywords) {
	
		List<String> list = new ArrayList<String>();
		list.add(keywords);
		List<Exercise> exercises = exerciseRepository.findByKeywords(list);

		return exercises;
	}
	
	@GetMapping(path = "/getTestExerciseBy4Values/{value}")
	public List<List> findByExercises4Values1(@RequestBody List<List<String>> value, @PathVariable("value") int page) {
		String parameter = value.get(0).get(0);
		List<String> list = value.get(1);
		String parameter2 = value.get(2).get(0);
		List<String> list2 = value.get(3);
		String parameter3 = value.get(4).get(0);
		List<String> list3 = value.get(5);
		String parameter4 = value.get(6).get(0);
		double total;
		List<List> listas = new ArrayList<List>();
		List<Double> total1 = new ArrayList<Double>();
		
		if(parameter4.equals("averageGrade") ) {
			String list4 = value.get(7).get(0);
			total = Math.ceil((double)exerciseRepository.findByExercises4ValuesCount(parameter, list, parameter2, list2, parameter3, list3, parameter4, list4)/10);
			total1.add(total);
			List<Exercise> tests = exerciseRepository.findByExercises4Values(parameter, list, parameter2, list2, parameter3, list3, parameter4, list4, PageRequest.of(page, 10));
			listas.add(tests);
		} else {
			List<String> list4 = value.get(7);
			total = Math.ceil((double)exerciseRepository.findByExercises4ValuesCount(parameter, list, parameter2, list2, parameter3, list3, parameter4, list4)/10);
			total1.add(total);
			List<Exercise> tests = exerciseRepository.findByExercises4Values(parameter, list, parameter2, list2, parameter3, list3, parameter4, list4, PageRequest.of(page, 10));
			listas.add(tests);
		}
		listas.add(total1);
		return listas;
	}
	
	
	@GetMapping(path = "/getTestExerciseBy3Values/{value}")
	public List<List> findByExercises3Values1(@RequestBody List<List<String>> value, @PathVariable("value") int page) {
		String parameter = value.get(0).get(0);
		List<String> list = value.get(1);
		String parameter2 = value.get(2).get(0);
		List<String> list2 = value.get(3);
		String parameter3 = value.get(4).get(0);
		double total;
		List<List> listas = new ArrayList<List>();
		List<Double> total1 = new ArrayList<Double>();
		
		if(parameter3.equals("averageGrade") ) {
			String list3 = value.get(5).get(0);
			total = Math.ceil((double)exerciseRepository.findByExercises3ValuesCount(parameter, list, parameter2, list2, parameter3, list3)/10);
			total1.add(total);
			List<Exercise> tests = exerciseRepository.findByExercises3Values(parameter, list, parameter2, list2, parameter3, list3, PageRequest.of(page, 10));
			listas.add(tests);
		}else {
			List<String> list3 = value.get(5);
			total = Math.ceil((double)exerciseRepository.findByExercises3ValuesCount(parameter, list, parameter2, list2, parameter3, list3)/10);
			total1.add(total);
			List<Exercise> tests = exerciseRepository.findByExercises3Values(parameter, list, parameter2, list2, parameter3, list3, PageRequest.of(page, 10));
			listas.add(tests);
		}
		listas.add(total1);
		return listas;
	}
	
	
	@GetMapping(path = "/getTestExerciseByValues/{value}")
	public List<List> findByExercises2Values1(@RequestBody List<List<String>> value, @PathVariable("value") int page) {
		String parameter = value.get(0).get(0);
		List<String> list = value.get(1);
		String parameter2 = value.get(2).get(0);
		double total;
		List<List> listas = new ArrayList<List>();
		List<Double> total1 = new ArrayList<Double>();
		
		if(parameter2.equals("averageGrade") ) {
			String list2 = value.get(3).get(0);
			total = Math.ceil((double)exerciseRepository.findByExercises2ValuesCount(parameter, list, parameter2, list2)/10);
			total1.add(total);
			List<Exercise> tests =exerciseRepository.findByExercises2Values(parameter, list, parameter2, list2, PageRequest.of(page, 10));
			listas.add(tests);
		}else {
			List<String> list2 = value.get(3);
			total = Math.ceil((double)exerciseRepository.findByExercises2ValuesCount(parameter, list, parameter2, list2)/10);
			total1.add(total);
			List<Exercise> tests = exerciseRepository.findByExercises2Values(parameter, list, parameter2, list2, PageRequest.of(page, 10));
			listas.add(tests);
		}
		listas.add(total1);
		return listas;
	}

	public static String capitalize(String str) {
		if(str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
	
	
	@GetMapping(path = "/getTestByValue/{value}")
	public List<List> getTestByValue(@RequestBody List<List<String>> value, @PathVariable("value") int page) {
		boolean score=false;
		double total;
		List<List> listas = new ArrayList<List>();
		List<Double> total1 = new ArrayList<Double>();
		
		for (List<String> list : value) {
			if(list.contains("averageGrade")) {
				score=true;
			}
		}
		String type = value.get(1).get(0);
	if( score) {
		String list = value.get(0).get(0);
		total = Math.ceil((double)exerciseRepository.QueryFindByValueCount( type, list)/10);
		total1.add(total);
		List<Exercise> tests = exerciseRepository.QueryFindByValue( type, list, PageRequest.of(page, 10));
		listas.add(tests);
	}else {
		List<String> list = value.get(0);
		total = Math.ceil((double)exerciseRepository.QueryFindByValueCount( type, list)/10);
		total1.add(total);
		List<Exercise> tests = exerciseRepository.QueryFindByValue( type, list, PageRequest.of(page, 10));
		listas.add(tests);
	}
	
	listas.add(total1);
	return listas;
	}

}
