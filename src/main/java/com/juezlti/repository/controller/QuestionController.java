package com.juezlti.repository.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juezlti.repository.models.Question;
import com.juezlti.repository.models.Test;
import com.juezlti.repository.repository.QuestionRepository;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/questions")
public class QuestionController {
	

	@Autowired
    private QuestionRepository questionRepository;
	
	
	@PostMapping(path = "/createQuestion")
	public String createQuestions(@RequestBody String questionJson) {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		List<Question> questions = new ArrayList<>();
		JSONArray jsonArray = new JSONArray();
		String jsonResponse="";
		Question createdQuestion;
		JSONObject jsonObject = null;

		try {
			questions = objectMapper.readValue(questionJson, new TypeReference<List<Question>>() {
			});
			for (Question q : questions) {
				try {
						
					createdQuestion = questionRepository.save(q);
					 jsonObject = new JSONObject(createdQuestion);

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

	@PostMapping(path = "/getAllQuestions")
	public List<Question> getAllQuestions(@RequestParam("questionIds") String questionIds) {
		List<String> questionsIdArr = Arrays.asList(questionIds.split(","));
		return questionRepository.findByIdIn(questionsIdArr).stream().map(el -> {
			boolean isSql = Optional.ofNullable(el.getQuestion_sql_type()).isPresent();
			boolean isCode = Optional.ofNullable(el.getQuestion_language()).isPresent();
			
			el.setCodeQuestion(!isSql && isCode);
			el.setSqlQuestion(isSql && !isCode);
			return el;
		}).collect(Collectors.toList());
	}
	
	@GetMapping(path = "/getAllQuestions/{value}")
	public List<List> getAllQuestionsPaged(@PathVariable("value") Integer page) {
		
		double total = Math.ceil((double)questionRepository.findAllQuestions()/10);
		List<List> listas = new ArrayList<List>();
		List<Double> total1 = new ArrayList<Double>();
		total1.add(total);
		List<Question> questions = questionRepository.findAllQuestions(PageRequest.of(page, 10));
		listas.add(questions);
		listas.add(total1);

		return listas;
	}
	
	@GetMapping(path = "/getKeywords/{value}")
	public List<Question> getKeywords(@PathVariable("value") String keywords) {
	
		List<String> list = new ArrayList<String>();
		list.add(keywords);
		List<Question> questions = questionRepository.findByKeywords(list);

		return questions;
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
		
		if(parameter4.equals("averageGrade") ) {
			String list4 = value.get(7).get(0);
			total = Math.ceil((double)questionRepository.findByQuestions4ValuesCount(parameter, list, parameter2, list2, parameter3, list3, parameter4, list4)/10);
			total1.add(total);
			List<Question> tests = questionRepository.findByQuestions4Values(parameter, list, parameter2, list2, parameter3, list3, parameter4, list4, PageRequest.of(page, 10));
			listas.add(tests);
		}else {
			List<String> list4 = value.get(7);
			total = Math.ceil((double)questionRepository.findByQuestions4ValuesCount(parameter, list, parameter2, list2, parameter3, list3, parameter4, list4)/10);
			total1.add(total);
			List<Question> tests = questionRepository.findByQuestions4Values(parameter, list, parameter2, list2, parameter3, list3, parameter4, list4, PageRequest.of(page, 10));
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
		
		if(parameter3.equals("averageGrade") ) {
			String list3 = value.get(5).get(0);
			total = Math.ceil((double)questionRepository.findByQuestions3ValuesCount(parameter, list, parameter2, list2, parameter3, list3)/10);
			total1.add(total);
			List<Question> tests = questionRepository.findByQuestions3Values(parameter, list, parameter2, list2, parameter3, list3, PageRequest.of(page, 10));
			listas.add(tests);
		}else {
			List<String> list3 = value.get(5);
			total = Math.ceil((double)questionRepository.findByQuestions3ValuesCount(parameter, list, parameter2, list2, parameter3, list3)/10);
			total1.add(total);
			List<Question> tests = questionRepository.findByQuestions3Values(parameter, list, parameter2, list2, parameter3, list3, PageRequest.of(page, 10));
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
		
		if(parameter2.equals("averageGrade") ) {
			String list2 = value.get(3).get(0);
			total = Math.ceil((double)questionRepository.findByQuestions2ValuesCount(parameter, list, parameter2, list2)/10);
			total1.add(total);
			List<Question> tests =questionRepository.findByQuestions2Values(parameter, list, parameter2, list2, PageRequest.of(page, 10));
			listas.add(tests);
		}else {
			List<String> list2 = value.get(3);
			total = Math.ceil((double)questionRepository.findByQuestions2ValuesCount(parameter, list, parameter2, list2)/10);
			total1.add(total);
			List<Question> tests = questionRepository.findByQuestions2Values(parameter, list, parameter2, list2, PageRequest.of(page, 10));
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
		total = Math.ceil((double)questionRepository.QueryFindByValueCount( type, list)/10);
		total1.add(total);
		List<Question> tests = questionRepository.QueryFindByValue( type, list, PageRequest.of(page, 10));
		listas.add(tests);
	}else {
		List<String> list = value.get(0);
		total = Math.ceil((double)questionRepository.QueryFindByValueCount( type, list)/10);
		total1.add(total);
		List<Question> tests = questionRepository.QueryFindByValue( type, list, PageRequest.of(page, 10));
		listas.add(tests);
	}
	
	listas.add(total1);
	return listas;
	}

}
