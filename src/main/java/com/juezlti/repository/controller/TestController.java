package com.juezlti.repository.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juezlti.repository.models.Exercise;
import com.juezlti.repository.models.Test;
import com.juezlti.repository.repository.ExerciseRepository;
import com.juezlti.repository.repository.TestRepository;
import com.juezlti.repository.util.JsonConverter;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/tests")
public class TestController {

	@Autowired
	private ExerciseRepository exerciseRepository;

	@Autowired
	private TestRepository testRepository;

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

	@GetMapping(path = "/getAllTest")
	public List<Test> getAllTest() {
		return testRepository.findAllTest();
	}

	@GetMapping(path = "/getAllTest/{value}")
	public List<List> getAllTest(@PathVariable("value") Integer page) {
		List<List> listas = new ArrayList<List>();
		List<Double> total1 = new ArrayList<Double>();
		double total = Math.ceil((double) testRepository.findAllTestCount() / 10);
		List<Test> tests = testRepository.findAllTest(PageRequest.of(page, 10));
		total1.add(total);
		listas.add(tests);
		listas.add(total1);

		return listas;
	}

	@GetMapping(path = "/getExercise/{value}")
	public Exercise getExerciseId(@PathVariable("value") String value) {
		return testRepository.findExerciseById(value);
	}

	@GetMapping(path = "/getExercise/id/{value}")
	public Exercise getExerciseAkId(@PathVariable("value") String value) {
		return testRepository.findExerciseByAkId(value);
	}

	@GetMapping(path = "/getTestId/{value}")
	public Optional<Test> getTestId(@PathVariable("value") String value) {

		return testRepository.findById(value);
	}

	@GetMapping(path = "/getTestExerciseId/{value}")
	public Test getTestExerciseId(@PathVariable("value") String value) {

		return testRepository.findByExerciseId(value);
	}

	@GetMapping(path = "/getTestExerciseId1/{value}")
	public Test getTestExerciseId1(@PathVariable("value") String value) {

		return testRepository.findByIdOrExercisesId(value, value);
	}

	@GetMapping(path = "/getTestExerciseType")
	public List<Test> getTestExerciseType(@RequestBody List<String> value) {

		return testRepository.findByExercisesTypeIn(value);
	}

	@GetMapping(path = "/getTestExerciseDifficulty")
	public List<Test> getTestExerciseDifficulty(@RequestBody List<String> value) {

		return testRepository.findByExercisesDifficultyIn(value);
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


	@GetMapping(path = "/type")
	public List<Exercise> getExercisesType(@RequestBody List<String> value) {

		return exerciseRepository.findByTypeInIgnoreCase(value);
	}

	@GetMapping(path = "/difficulty")
	public List<Exercise> getExercisesDifficulty(@RequestBody List<String> value) {
		return exerciseRepository.findByDifficultyInIgnoreCase(value);
	}

	@GetMapping(path = "/difficultyType")
	public List<Exercise> getExercisesDifficultyType(@RequestBody List<List<String>> value) {
		List<String> difficulty = value.get(0);
		List<String> type = value.get(1);
		return exerciseRepository.findByDifficultyInIgnoreCaseAndTypeInIgnoreCase(difficulty, type);
	}

	@GetMapping(path = "/title/{value}")
	public List<Exercise> getExercisesTitle(@PathVariable("value") String value) {

		return exerciseRepository.findByTitleLikeIgnoreCase(value);
	}

	@DeleteMapping(path = "/delete/{value}")
	public void deleteExercise(@PathVariable("value") String value) {

		exerciseRepository.deleteById(value);
	}

}
