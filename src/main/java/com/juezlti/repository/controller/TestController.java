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
import com.juezlti.repository.models.Question;
import com.juezlti.repository.models.Test;
import com.juezlti.repository.repository.QuestionRepository;
import com.juezlti.repository.repository.TestRepository;
import com.juezlti.repository.util.JsonConverter;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/tests")
public class TestController {

	@Autowired
	private QuestionRepository questionRepository;

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
					List<Question> questions = t.getQuestions();

					for (int i = 0; i < questions.size(); i++) {
						questions.get(i).setId(new ObjectId().toString());
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
					newTest.setQuestions(questions);
					createdTest = testRepository.save(newTest);
					jsonObject = new JSONObject(createdTest);
				} catch (Exception ex) {
					log.error("Unexpected error trying to create question {}", ex);
					return new String("Unexpected error trying to create question " + HttpStatus.BAD_REQUEST);
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

	@GetMapping(path = "/getQuestion/{value}")
	public Question getQuestionId(@PathVariable("value") String value) {
		return testRepository.findQuestionById(value);
	}

	@GetMapping(path = "/getTestId/{value}")
	public Optional<Test> getTestId(@PathVariable("value") String value) {

		return testRepository.findById(value);
	}

	@GetMapping(path = "/getTestQuestionId/{value}")
	public Test getTestQuestionId(@PathVariable("value") String value) {

		return testRepository.findByQuestionId(value);
	}

	@GetMapping(path = "/getTestQuestionId1/{value}")
	public Test getTestQuestionId1(@PathVariable("value") String value) {

		return testRepository.findByIdOrQuestionsId(value, value);
	}

	@GetMapping(path = "/getTestQuestionType")
	public List<Test> getTestQuestionType(@RequestBody List<String> value) {

		return testRepository.findByQuestionsTypeIn(value);
	}

	@GetMapping(path = "/getTestQuestionDifficulty")
	public List<Test> getTestQuestionDifficulty(@RequestBody List<String> value) {

		return testRepository.findByQuestionsDifficultyIn(value);
	}

	@GetMapping(path = "/getTestQuestionBy4Values/{value}")
	public List<List> findByQuestions4Values1(@RequestBody List<List<String>> value, @PathVariable("value") int page) {
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
			total = Math.ceil((double) testRepository.findByQuestions4ValuesCount(parameter, list, parameter2, list2,
					parameter3, list3, parameter4, list4) / 10);
			total1.add(total);
			List<Test> tests = testRepository.findByQuestions4Values(parameter, list, parameter2, list2, parameter3,
					list3, parameter4, list4, PageRequest.of(page, 10));
			listas.add(tests);
		} else {
			List<String> list4 = value.get(7);
			total = Math.ceil((double) testRepository.findByQuestions4ValuesCount(parameter, list, parameter2, list2,
					parameter3, list3, parameter4, list4) / 10);
			total1.add(total);
			List<Test> tests = testRepository.findByQuestions4Values(parameter, list, parameter2, list2, parameter3,
					list3, parameter4, list4, PageRequest.of(page, 10));
			listas.add(tests);
		}
		listas.add(total1);
		return listas;
	}

	@GetMapping(path = "/getTestQuestionBy3Values/{value}")
	public List<List> findByQuestions3Values1(@RequestBody List<List<String>> value, @PathVariable("value") int page) {
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
			total = Math.ceil((double) testRepository.findByQuestions3ValuesCount(parameter, list, parameter2, list2,
					parameter3, list3) / 10);
			total1.add(total);
			List<Test> tests = testRepository.findByQuestions3Values(parameter, list, parameter2, list2, parameter3,
					list3, PageRequest.of(page, 10));
			listas.add(tests);
		} else {
			List<String> list3 = value.get(5);
			total = Math.ceil((double) testRepository.findByQuestions3ValuesCount(parameter, list, parameter2, list2,
					parameter3, list3) / 10);
			total1.add(total);
			List<Test> tests = testRepository.findByQuestions3Values(parameter, list, parameter2, list2, parameter3,
					list3, PageRequest.of(page, 10));
			listas.add(tests);
		}
		listas.add(total1);
		return listas;
	}

	@GetMapping(path = "/getTestQuestionByValues/{value}")
	public List<List> findByQuestions2Values1(@RequestBody List<List<String>> value, @PathVariable("value") int page) {
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
					.ceil((double) testRepository.findByQuestions2ValuesCount(parameter, list, parameter2, list2) / 10);
			total1.add(total);
			List<Test> tests = testRepository.findByQuestions2Values(parameter, list, parameter2, list2,
					PageRequest.of(page, 10));
			listas.add(tests);
		} else {
			List<String> list2 = value.get(3);
			total = Math
					.ceil((double) testRepository.findByQuestions2ValuesCount(parameter, list, parameter2, list2) / 10);
			total1.add(total);
			List<Test> tests = testRepository.findByQuestions2Values(parameter, list, parameter2, list2,
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
	public List<Question> getQuestionsType(@RequestBody List<String> value) {

		return questionRepository.findByTypeInIgnoreCase(value);
	}

	@GetMapping(path = "/difficulty")
	public List<Question> getQuestionsDifficulty(@RequestBody List<String> value) {
		return questionRepository.findByDifficultyInIgnoreCase(value);
	}

	@GetMapping(path = "/difficultyType")
	public List<Question> getQuestionsDifficultyType(@RequestBody List<List<String>> value) {
		List<String> difficulty = value.get(0);
		List<String> type = value.get(1);
		return questionRepository.findByDifficultyInIgnoreCaseAndTypeInIgnoreCase(difficulty, type);
	}

	@GetMapping(path = "/title/{value}")
	public List<Question> getQuestionsTitle(@PathVariable("value") String value) {

		return questionRepository.findByTitleLikeIgnoreCase(value);
	}

	@DeleteMapping(path = "/delete/{value}")
	public void deleteQuestion(@PathVariable("value") String value) {

		questionRepository.deleteById(value);
	}

}
