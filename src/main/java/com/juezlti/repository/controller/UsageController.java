package com.juezlti.repository.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juezlti.repository.models.Usage;
import com.juezlti.repository.models.Exercise;
import com.juezlti.repository.models.Test;
import com.juezlti.repository.models.User;
import com.juezlti.repository.repository.UsageRepository;
import com.juezlti.repository.repository.ExerciseRepository;
import com.juezlti.repository.repository.TestRepository;
import com.juezlti.repository.util.JsonConverter;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/usage")
public class UsageController {

	@Autowired
	private UsageRepository usageRepository;
	
	@Autowired
	private ExerciseRepository exerciseRepository;
	
	@Autowired
	private TestRepository testRepository;

	
	@PostMapping(path = "/tickets")
	public String createUsage(@RequestBody String usageJson) {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		Usage usages = new Usage();
		JSONObject jsonArray = new JSONObject();
		String jsonResponse;

		try {
			usages = objectMapper.readValue(usageJson, new TypeReference<Usage>() {
			});
			
				try {
					String idExercise = usages.getIdExercise();
					String ctId = usages.getCtId();
					LocalDate currentDate = LocalDate.now();
					User user = usages.getUser();
					Integer timeScore = usages.getTimeScore();
					Integer understandabilityScore = usages.getUnderstandabilityScore();
					Integer difficultyScore = usages.getDifficultyScore();

					if (StringUtils.isBlank(idExercise) || timeScore == null || understandabilityScore==null || difficultyScore==null) {
						JSONObject converted = JsonConverter.failConverter(HttpStatus.BAD_REQUEST, "Empty field", usages);
						log.warn("Empty field");
					}

					if ((timeScore< 1 || timeScore > 5) || (understandabilityScore< 1 || understandabilityScore > 5) || (difficultyScore< 1 || difficultyScore > 5)) {
						log.warn("Incorrect score {}");
						JSONObject converted = JsonConverter.failConverter(HttpStatus.BAD_REQUEST, "Incorrect score",
								usages);
					}

					Usage newUsage = new Usage();
					newUsage.setIdExercise(idExercise);
					newUsage.setCtId(ctId);
					newUsage.setDate(currentDate);
					newUsage.setUnderstandabilityScore(understandabilityScore);
					newUsage.setDifficultyScore(difficultyScore);
					newUsage.setTimeScore(timeScore);
					newUsage.setUser(user);
					usageRepository.save(newUsage);

					JSONObject converted = JsonConverter.okConverter(HttpStatus.OK, usages);

				} catch (Exception ex) {
					log.error("Unexpected error trying to create exercise {}", ex);
					return new String("Unexpected error trying to create exercise " + HttpStatus.BAD_REQUEST);
				}
			
			jsonResponse = jsonArray.toString();

		} catch (JsonProcessingException e) {
			log.warn("Failure processing JSON", e);
			return new String("Failure processing JSON " + HttpStatus.BAD_REQUEST);
		}
		return jsonResponse;
	}

	@GetMapping(path = "/date/{value}")
	public List<Usage> getUsageDate(@PathVariable("value") String value) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Date date=null;
		try {
			date = format.parse(value);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return usageRepository.findByDate(date);
	}
	
	@GetMapping(path = "/exercise_id/{value}")
	public List<Usage> getUsageExerciseId(@PathVariable("value") String value) {

		return usageRepository.findByIdExerciseIgnoreCase(value);
	}

	@GetMapping(path = "/usagesCount")
	public int get(@RequestParam("ctid") String ctId) {
		return usageRepository.countByCtId(ctId);
	}
	

	
	@GetMapping(path = "/getUsageByIds")
	public List<Usage> getUsageByIds(@RequestBody List<List<String>> value) {
		List<String> list = value.get(0);
		List<String> list2 = value.get(1);
		String ctId = value.get(2).get(0);
		
		return usageRepository.findByIdExerciseInAndIdInAndCtId( list,  list2, ctId);
	}
	
	@PutMapping(path = "/updateTest")
	public ResponseEntity updateTest(@RequestBody String json) {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		Test test = new Test();
		JSONObject jsonArray = new JSONObject();
		String jsonResponse;
		
		try {
			test = objectMapper.readValue(json, new TypeReference<Test>() {
			});
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Test t = testRepository.save(test);
		return ResponseEntity.status(HttpStatus.OK).body(t.toString());
	}
	
	@PutMapping(path = "/updateExercise")
	public ResponseEntity updateExercise(@RequestBody String json) {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		Usage usage= new Usage();
		
		try {
			usage = objectMapper.readValue(json, new TypeReference<Usage>() {
			});
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Exercise newExercise = testRepository.findExerciseById(usage.getIdExercise());
	
			if(newExercise.getNumberVotes()!=null) {
				int nVotes =Integer.valueOf(newExercise.getNumberVotes());
				newExercise.setNumberVotes(String.valueOf(nVotes+1));
				newExercise.setAverageGradeUnderstability(String.valueOf( ( (nVotes*Double.valueOf(newExercise.getAverageGradeUnderstability())) +  Double.valueOf(usage.getUnderstandabilityScore()) ) / (nVotes+1)));
				newExercise.setAverageGradeDifficulty(String.valueOf( ( (nVotes*Double.valueOf(newExercise.getAverageGradeDifficulty())) +  Double.valueOf(usage.getDifficultyScore()) ) / (nVotes+1)));
				newExercise.setAverageGradeTime(String.valueOf( ( (nVotes*Double.valueOf(newExercise.getAverageGradeTime())) +  Double.valueOf(usage.getTimeScore()) ) / (nVotes+1)));
				newExercise.setAverageGrade(String.valueOf( (  Double.valueOf(newExercise.getAverageGradeUnderstability())+Double.valueOf(newExercise.getAverageGradeDifficulty()) + Double.valueOf(newExercise.getAverageGradeTime())  ) / 3));
			}else {
				newExercise.setNumberVotes("1");
				newExercise.setAverageGradeUnderstability(String.valueOf(Double.valueOf(usage.getUnderstandabilityScore())));
				newExercise.setAverageGradeDifficulty(String.valueOf(Double.valueOf(usage.getDifficultyScore()) ));
				newExercise.setAverageGradeTime(String.valueOf(Double.valueOf(usage.getTimeScore())));
				newExercise.setAverageGrade(String.valueOf( (  Double.valueOf(usage.getUnderstandabilityScore())+Double.valueOf(usage.getDifficultyScore()) + Double.valueOf(usage.getTimeScore())  ) / 3));
			}
		Exercise q =exerciseRepository.save(newExercise);

		return ResponseEntity.status(HttpStatus.OK).body(q.toString());
	}

}
