package com.juezlti.repository.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.juezlti.repository.storage.FileService;
import com.juezlti.repository.storage.FilesController;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.lingala.zip4j.ZipFile;
import org.json.JSONArray;
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
	private ExerciseService exerciseService;
	
	public enum ExerciseData {
		STATEMENT,
		TEST,
		SOLUTION
	}

	@PostMapping(path = "/createExercise")
	public String createExercises(@RequestBody String exerciseJson) {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		List<Exercise> exercises = new ArrayList<>();
		JSONArray jsonArray = new JSONArray();
		String jsonResponse="";
		Exercise createdExercise;
		JSONObject jsonObject = null;

		try {
			exercises = objectMapper.readValue(exerciseJson, new TypeReference<List<Exercise>>() {
			});
			for (Exercise q : exercises) {
				try {
						
					createdExercise = exerciseRepository.save(q);
					 jsonObject = new JSONObject(createdExercise);

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

	@PostMapping("import-file")
	public ResponseEntity<FilesController.UploadResponseMessage> uploadFile(
			@RequestParam("exercise") MultipartFile file
	) {
		try {
			Path savedPath = fileService.save(file, uploadPath);
			
			ZipFile zipFile = new ZipFile(savedPath.toFile());
			zipFile.extractAll(
				fileService.getBaseUploadStrPath() +
							exercisesPath +
							"/" +
							savedPath.getFileName().toString().replace(".zip", "")
			);
			
			return ResponseEntity.status(HttpStatus.OK)
					.body(new FilesController.UploadResponseMessage("Uploaded the file successfully: " + file.getOriginalFilename()));
		} catch (Exception e) {
			System.out.println("FAILED");
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new FilesController.UploadResponseMessage("Could not upload the file: " + file.getOriginalFilename() + "!"));
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
		}else {
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
