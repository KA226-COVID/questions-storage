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
import com.juezlti.repository.models.Exercise;
import com.juezlti.repository.models.Test;
import com.juezlti.repository.repository.ExerciseRepository;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/exercises")
public class ExerciseController {
	

	@Autowired
    private ExerciseRepository exerciseRepository;
	
	
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

	@PostMapping(path = "/getAllExercises")
	public List<Exercise> getAllExercises(@RequestParam("exerciseIds") String exerciseIds) {
		List<String> exercisesIdArr = Arrays.asList(exerciseIds.split(","));
		return exerciseRepository.findByIdIn(exercisesIdArr).stream().map(el -> {
			boolean isSql = Optional.ofNullable(el.getExercise_sql_type()).isPresent();
			boolean isCode = Optional.ofNullable(el.getExercise_language()).isPresent();
			
			el.setCodeExercise(!isSql && isCode);
			el.setSqlExercise(isSql && !isCode);
			return el;
		}).collect(Collectors.toList());
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
