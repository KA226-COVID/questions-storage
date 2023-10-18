package com.juezlti.repository.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.juezlti.repository.models.yapexil.ExerciseMetadata;
import com.juezlti.repository.models.yapexil.SolutionMetadata;
import com.juezlti.repository.models.yapexil.StatementMetadata;
import com.juezlti.repository.models.yapexil.TestMetadata;
import com.juezlti.repository.models.yapexil.LibraryMetadata;
import com.juezlti.repository.storage.FileService;
import com.juezlti.repository.util.HtmlFilter;
import com.juezlti.repository.util.JsonConverter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lingala.zip4j.ZipFile;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juezlti.repository.models.Exercise;
import com.juezlti.repository.models.Test;
import com.juezlti.repository.repository.ExerciseRepository;
import com.juezlti.repository.repository.TestRepository;
import com.juezlti.repository.repository.UsageRepository;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
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
	private UsageRepository usageRepository;

	@Autowired
	private FileService fileService;

	@Autowired
	private HtmlFilter htmlFilterFactory;

	@Autowired
	private TestRepository testRepository;

	public enum ExerciseData {
		STATEMENT,
		TEST,
		SOLUTION,
		LIBRARIES
	}

	//CT_MAIN
	@PostMapping(path = "/createExercise")
	public String createExercises(@RequestParam(name = "json") String exerciseJson,
								  @RequestParam(name = "file_field", required = false) List<MultipartFile> files,
								  @RequestParam(name = "recuperated_libraries", required = false) String recuperatedLibraries) {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		List<Exercise> exercises = new ArrayList<>();
		log.info("Received JSON: "+ exerciseJson);
		String jsonResponse="";
		JSONObject jsonResult = null;
		List<String> recLibraries = new ArrayList<>();

		try {
			exercises = objectMapper.readValue(exerciseJson, new TypeReference<List<Exercise>>() {
			});
			for (Exercise receivedExercise : exercises) {
				try {
					if(files != null && files.size() > 0) {
						receivedExercise.setExercise_libraries(files);
					}
					if(recuperatedLibraries != null) { // The colon separator indicate that the first one is the exercise_id and the second one is the library_id (1212adsdad-asd:122saxcz)
						recLibraries = objectMapper.readValue(recuperatedLibraries, new TypeReference<List<String>>(){});
					}
					boolean updateExercise = false;
					try{
						if(!StringUtils.isEmpty(receivedExercise.getId())){
							updateExercise = (usageRepository.countByIdExerciseIgnoreCase(receivedExercise.getId()) > 0);
						}
					}catch(Exception e){
						e.printStackTrace();
					}
					receivedExercise.setCodeExercise(true);
					jsonResult = fileService.generateMetadatas(receivedExercise, recLibraries, updateExercise);
					log.info("Exercise: " + receivedExercise);
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
	//Download-ak-exercise
	@PostMapping("import-file")
	public ResponseEntity<String> uploadFile(
			@RequestParam("exercise") MultipartFile file,
			@RequestParam("PHPSESSID") String phpSessionId,
			@RequestParam("sessionLanguage") String sessionLanguage
	) {
		Exercise savedExercise;
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

			savedExercise = createRepositoryExercise(repositoryExercise.orElse(new Exercise(akId, sessionLanguage)));
			return ResponseEntity.status(HttpStatus.OK).body(savedExercise.getId());

		} catch (Exception e) {
			log.warn("FAILED");
			log.warn(e.getMessage());
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

	//Download-ak-Exercise
	@GetMapping("{id}/export")
	public ResponseEntity<Resource> exportExerciseZip(
			@PathVariable String id, HttpServletRequest request
	){
		Resource file = fileService.load(id+".zip");
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
	//Student-home
	@GetMapping("{id}/statements")
	public ResponseEntity<List<StatementMetadata>> getExerciseStatements(
			@PathVariable String id, HttpServletRequest request
	){
		return ResponseEntity.ok()
				.body(fileService.getExerciseStatementsMetadata(id, null));
	}
	//Student-home
	@GetMapping("{id}/statements/{lang}")
	public ResponseEntity<List<StatementMetadata>> getExerciseStatements(
			@PathVariable String id, @PathVariable String lang, HttpServletRequest request
	){
		return ResponseEntity.ok()
				.body(fileService.getExerciseStatementsMetadata(id, lang));
	}
	//Student-home
	@GetMapping("{id}/tests")
	public ResponseEntity<List<TestMetadata>> getExerciseTests(
			@PathVariable String id, HttpServletRequest request
	){
		return ResponseEntity.ok()
				.body(fileService.getExerciseTestMetadata(id));
	}

	//CT_EXERCISE
	@GetMapping("{id}/libraries")
	public ResponseEntity<List<LibraryMetadata>> getExerciseLibraries(
			@PathVariable String id, HttpServletRequest request
	){
		return ResponseEntity.ok().body(fileService.getExerciseLibrariesMetadata(id));
	}

	// Export-context 
	@PostMapping(path = "/getAllExercises")
	public List<Exercise> getAllExercises(@RequestParam("exerciseIds") String exerciseIds) {
		List<String> exercisesIdArr = Arrays.asList(exerciseIds.split(","));
		return exerciseRepository.findByIdIn(exercisesIdArr);
	}
	//Ct_EXERCISE
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

	public StatementMetadata searchFirstStatement(String sessionLanguage, List<StatementMetadata> statementsMetadata, String akId){
		return searchFirstStatement(sessionLanguage, statementsMetadata, akId, "en");
	}

	public static String capitalize(String str) {
		if(str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}


	public StatementMetadata searchFirstStatement(String sessionLanguage, List<StatementMetadata> statementsMetadata, String akId, String fallbackLanguage){
		try {
			statementsMetadata = fileService.getExerciseStatementsMetadata(akId);
			StatementMetadata firstStatement = statementsMetadata
									.stream()
									.filter(el -> sessionLanguage.equals(el.getNat_lang()))
									.findFirst()
									.get();

									return firstStatement;
		} catch(Exception i){
			if(fallbackLanguage != null){
				return searchFirstStatement(fallbackLanguage, statementsMetadata, akId, null);
			}
			statementsMetadata = fileService.getExerciseStatementsMetadata(akId);
			StatementMetadata firstStatement = statementsMetadata
								.stream()
								.findFirst()
								.get();
			return firstStatement;
		}

	}

	public Exercise createRepositoryExercise(Exercise akExercise){

		String akId = akExercise.getAkId();
		String sessionLanguage = akExercise.getSessionLanguage();
		
		ExerciseMetadata exMetadata = fileService.getExerciseMetadata(akId);
		List<StatementMetadata> statementsMetadata = fileService.getExerciseStatementsMetadata(akId);
		List<SolutionMetadata> solutionsMetadata = fileService.getExerciseSolutionsMetadata(akId);
		List<TestMetadata> testMetadatas = fileService.getExerciseTestMetadata(akId);
		
		akExercise.setCodeExercise(false);

		SolutionMetadata firstSolution = solutionsMetadata
				.stream()
				.findFirst()
				.get();
		akExercise.setExercise_solution(fileService.readFileContentAsString(Paths.get(fileService.getBaseUploadStrPath(), fileService.getExercisesStrPath(), firstSolution.getFileStringPath())));
		
		Integer cont = 1;
		Map<String, String> exercise_input_test = new HashMap<String, String>();
		Map<String, String> exercise_output_test = new HashMap<String, String>();
		for (TestMetadata testMetadata : testMetadatas){
			exercise_input_test.put(cont.toString(), testMetadata.getInputValue());
			exercise_output_test.put(cont.toString(), testMetadata.getOutputValue());
			cont++;
		}
		akExercise.setExercise_input_test(exercise_input_test);
		akExercise.setExercise_output_test(exercise_output_test);

		StatementMetadata firstStatement = searchFirstStatement(sessionLanguage,statementsMetadata,akId);

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
		akExercise.setDifficulty(capitalize(exMetadata.getDifficulty().toLowerCase()));

		akExercise.setExercise_language(firstSolution.getLang().toLowerCase());

		Exercise savedExercise = exerciseRepository.save(akExercise);

		return savedExercise;
	}

		//CT_MAIN
		@PostMapping(path = "/createTest")
		public String createTests(@RequestBody String testJson) {
			ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			List<Test> tests = new ArrayList<>();
			JSONArray jsonArray = new JSONArray();
			String jsonResponse = "";
			Test createdTest;
			JSONObject jsonObject = null;
	
			try {
				tests = objectMapper.readValue(testJson, new TypeReference<List<Test>>() {
				});
				for (Test t : tests) {
					try {
						String description = t.getDescription();
						String name = t.getName();
						String status = t.getStatus();
						List<Exercise> exercises = t.getExercises();
	
						for (int i = 0; i < exercises.size(); i++) {
							exercises.get(i).setId(new ObjectId().toString());
						}
	
						if (StringUtils.isBlank(name) || StringUtils.isBlank(description)) {
							JSONObject converted = JsonConverter.failConverter(HttpStatus.BAD_REQUEST, "Empty field", t);
							log.warn("Empty field");
							jsonArray.put(converted);
							continue;
						}
	
						Test newTest = new Test();
						newTest.setName(name);
						newTest.setDescription(description);
						newTest.setStatus(status);
						newTest.setExercises(exercises);
						createdTest = testRepository.save(newTest);
						jsonObject = new JSONObject(createdTest);
					} catch (Exception ex) {
						log.error("Unexpected error trying to create exercise {}", ex);
						return new String("Unexpected error trying to create exercise " + HttpStatus.BAD_REQUEST);
					}
				}
	
				jsonResponse = jsonObject.toString();
			} catch (JsonProcessingException e) {
				log.warn("Failure processing JSON", e);
				return new String("Failure processing JSON " + HttpStatus.BAD_REQUEST);
			}
			return jsonResponse;
		}
	
		//CT_TEST
		@GetMapping(path = "/getAllTest")
		public List<Test> getAllTest() {
			return testRepository.findAllTest();
		}
		//CT_TEST
		@GetMapping(path = "/getAllTest/{value}")
		public List<List> getAllTest(@PathVariable("value") Integer page) {
			List<List> listas = new ArrayList<List>();
			List<Double> total1 = new ArrayList<Double>();
			double total = Math.ceil((double) testRepository.findAllTestCount() / 10);
			
			//List<Test> tests = testRepository.findAllTest(PageRequest.of(page, 10));
			List<Test> tests = testRepository.findAllTest(PageRequest.of(page, 10));
			log.info("testRepository.findAllTestCount()" + tests);
			total1.add(total);
			listas.add(tests);
			listas.add(total1);
	
			return listas;
		}
	
		//CT_EXERCISE
		@GetMapping(path = "/getExercise/{value}")
		public Exercise getExerciseId(@PathVariable("value") String value) {
			return testRepository.findExerciseById(value);
		}
	
		//CT_EXERCISE
		@GetMapping(path = "/getExercise/id/{value}")
		public Exercise getExerciseAkId(@PathVariable("value") String value) {
			return testRepository.findExerciseByAkId(value);
		}
	
		//CT_EXERCISE CT_TEST
		@GetMapping(path = "/getTestId/{value}")
		public Optional<Test> getTestId(@PathVariable("value") String value) {
	
			return testRepository.findById(value);
		}
	
		//CT_USAGE
		@GetMapping(path = "/getTestExerciseId1/{value}")
		public Test getTestExerciseId1(@PathVariable("value") String value) {
	
			return testRepository.findByIdOrExercisesId(value, value);
		}
	
		//CT_TEST
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
	
			//if the last parameter is averageGrade take it as String else take it as List
			if (parameter4.equals("averageGrade")) {
				String list4 = value.get(7).get(0);
				total = Math.ceil((double) testRepository.findByExercises4ValuesCount(parameter, list, parameter2, list2,
						parameter3, list3, parameter4, list4) / 10);
				total1.add(total);
				List<Test> tests = testRepository.findByExercises4Values(parameter, list, parameter2, list2, parameter3,
						list3, parameter4, list4, PageRequest.of(page, 10));
				listas.add(tests);
			} else {
				List<String> list4 = value.get(7);
				total = Math.ceil((double) testRepository.findByExercises4ValuesCount(parameter, list, parameter2, list2,
						parameter3, list3, parameter4, list4) / 10);
				total1.add(total);
				List<Test> tests = testRepository.findByExercises4Values(parameter, list, parameter2, list2, parameter3,
						list3, parameter4, list4, PageRequest.of(page, 10));
				listas.add(tests);
			}
			listas.add(total1);
			return listas;
		}
	
		//CT_TEST
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
	
			//if the last parameter is averageGrade take it as String else take it as List
			if (parameter3.equals("averageGrade")) {
				String list3 = value.get(5).get(0);
				total = Math.ceil((double) testRepository.findByExercises3ValuesCount(parameter, list, parameter2, list2,
						parameter3, list3) / 10);
				total1.add(total);
				List<Test> tests = testRepository.findByExercises3Values(parameter, list, parameter2, list2, parameter3,
						list3, PageRequest.of(page, 10));
				listas.add(tests);
			} else {
				List<String> list3 = value.get(5);
				total = Math.ceil((double) testRepository.findByExercises3ValuesCount(parameter, list, parameter2, list2,
						parameter3, list3) / 10);
				total1.add(total);
				List<Test> tests = testRepository.findByExercises3Values(parameter, list, parameter2, list2, parameter3,
						list3, PageRequest.of(page, 10));
				listas.add(tests);
			}
			listas.add(total1);
			return listas;
		}
	
		//CT_TEST
		@GetMapping(path = "/getTestExerciseByValues/{value}")
		public List<List> findByExercises2Values1(@RequestBody List<List<String>> value, @PathVariable("value") int page) {
			String parameter = value.get(0).get(0);
			List<String> list = value.get(1);
			String parameter2 = value.get(2).get(0);
			double total;
			List<List> listas = new ArrayList<List>();
			List<Double> total1 = new ArrayList<Double>();
	
			//if the last parameter is averageGrade take it as String else take it as List
			if (parameter2.equals("averageGrade")) {
				String list2 = value.get(3).get(0);
				total = Math
						.ceil((double) testRepository.findByExercises2ValuesCount(parameter, list, parameter2, list2) / 10);
				total1.add(total);
				List<Test> tests = testRepository.findByExercises2Values(parameter, list, parameter2, list2,
						PageRequest.of(page, 10));
				listas.add(tests);
			} else {
				List<String> list2 = value.get(3);
				total = Math
						.ceil((double) testRepository.findByExercises2ValuesCount(parameter, list, parameter2, list2) / 10);
				total1.add(total);
				List<Test> tests = testRepository.findByExercises2Values(parameter, list, parameter2, list2,
						PageRequest.of(page, 10));
				listas.add(tests);
			}
			listas.add(total1);
			return listas;
		}
	
		//CT_TEST
		@GetMapping(path = "/getTestByValue/{value}")
		public List<List> getTestByValue(@RequestBody List<List<String>> value, @PathVariable("value") int page) {
			boolean score = false;
			double total;
			List<List> listas = new ArrayList<List>();
			List<Double> total1 = new ArrayList<Double>();
	
			//if the parameter is averageGrade take it as String else take it as List
			for (List<String> list : value) {
				if (list.contains("averageGrade")) {
					score = true;
				}
			}
			String type = value.get(1).get(0);
			
			if (score) {
				String list = value.get(0).get(0);
				total = Math.ceil((double) testRepository.QueryFindByValueCount(type, list) / 10);
				total1.add(total);
				List<Test> tests = testRepository.QueryFindByValue(type, list, PageRequest.of(page, 10));
				listas.add(tests);
			} else {
				List<String> list = value.get(0);
				total = Math.ceil((double) testRepository.QueryFindByValueCount(type, list) / 10);
				total1.add(total);
				List<Test> tests = testRepository.QueryFindByValue(type, list, PageRequest.of(page, 10));
				listas.add(tests);
			}
			
			listas.add(total1);
			return listas;
		}

}
