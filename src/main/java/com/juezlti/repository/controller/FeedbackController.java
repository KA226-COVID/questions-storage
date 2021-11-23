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
import com.juezlti.repository.models.Feedback;
import com.juezlti.repository.models.Question;
import com.juezlti.repository.models.Test;
import com.juezlti.repository.models.User;
import com.juezlti.repository.repository.FeedbackRepository;
import com.juezlti.repository.repository.QuestionRepository;
import com.juezlti.repository.repository.TestRepository;
import com.juezlti.repository.util.JsonConverter;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/feedback")
public class FeedbackController {

	@Autowired
	private FeedbackRepository feedbackRepository;
	
	@Autowired
	private QuestionRepository questionRepository;
	
	@Autowired
	private TestRepository testRepository;

	
	@PostMapping(path = "/tickets")
	public String createFeedback(@RequestBody String feedbackJson) {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		Feedback feedbacks = new Feedback();
		JSONObject jsonArray = new JSONObject();
		String jsonResponse;

		try {
			feedbacks = objectMapper.readValue(feedbackJson, new TypeReference<Feedback>() {
			});
			
				try {
					String idQuestion = feedbacks.getIdQuestion();
					String ctId = feedbacks.getCtId();
					LocalDate currentDate = LocalDate.now();
					User user = feedbacks.getUser();
					Integer timeScore = feedbacks.getTimeScore();
					Integer understandabilityScore = feedbacks.getUnderstandabilityScore();
					Integer difficultyScore = feedbacks.getDifficultyScore();

					if (StringUtils.isBlank(idQuestion) || timeScore == null || understandabilityScore==null || difficultyScore==null) {
						JSONObject converted = JsonConverter.failConverter(HttpStatus.BAD_REQUEST, "Empty field", feedbacks);
						log.warn("Empty field");
					}

					if ((timeScore< 1 || timeScore > 5) || (understandabilityScore< 1 || understandabilityScore > 5) || (difficultyScore< 1 || difficultyScore > 5)) {
						log.warn("Incorrect score {}");
						JSONObject converted = JsonConverter.failConverter(HttpStatus.BAD_REQUEST, "Incorrect score",
								feedbacks);
					}

					Feedback newFeedback = new Feedback();
					newFeedback.setIdQuestion(idQuestion);
					newFeedback.setCtId(ctId);
					newFeedback.setDate(currentDate);
					newFeedback.setUnderstandabilityScore(understandabilityScore);
					newFeedback.setDifficultyScore(difficultyScore);
					newFeedback.setTimeScore(timeScore);
					newFeedback.setUser(user);
					feedbackRepository.save(newFeedback);

					JSONObject converted = JsonConverter.okConverter(HttpStatus.OK, feedbacks);

				} catch (Exception ex) {
					log.error("Unexpected error trying to create question {}", ex);
					return new String("Unexpected error trying to create question " + HttpStatus.BAD_REQUEST);
				}
			
			jsonResponse = jsonArray.toString();

		} catch (JsonProcessingException e) {
			log.warn("Failure processing JSON", e);
			return new String("Failure processing JSON " + HttpStatus.BAD_REQUEST);
		}
		return jsonResponse;
	}

	@GetMapping(path = "/date/{value}")
	public List<Feedback> getFeedbackDate(@PathVariable("value") String value) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		Date date=null;
		try {
			date = format.parse(value);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return feedbackRepository.findByDate(date);
	}
	
	@GetMapping(path = "/question_id/{value}")
	public List<Feedback> getFeedbackQuestionId(@PathVariable("value") String value) {

		return feedbackRepository.findByIdQuestionIgnoreCase(value);
	}

	@GetMapping(path = "/feedbacksCount")
	public int get(@RequestParam("ctid") String ctId) {
		return feedbackRepository.countByCtId(ctId);
	}
	

	
	@GetMapping(path = "/getFeedbackByIds")
	public List<Feedback> getFeedbackByIds(@RequestBody List<List<String>> value) {
		List<String> list = value.get(0);
		List<String> list2 = value.get(1);
		String ctId = value.get(2).get(0);
		
		return feedbackRepository.findByIdQuestionInAndUserIdInAndCtId( list,  list2, ctId);
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
	
	@PutMapping(path = "/updateQuestion")
	public ResponseEntity updateQuestion(@RequestBody String json) {
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		Feedback feedback= new Feedback();
		
		try {
			feedback = objectMapper.readValue(json, new TypeReference<Feedback>() {
			});
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Question newQuestion = testRepository.findQuestionById(feedback.getIdQuestion());
	
			if(newQuestion.getNumberVotes()!=null) {
				int nVotes =Integer.valueOf(newQuestion.getNumberVotes());
				newQuestion.setNumberVotes(String.valueOf(nVotes+1));
				newQuestion.setAverageGradeUnderstability(String.valueOf( ( (nVotes*Double.valueOf(newQuestion.getAverageGradeUnderstability())) +  Double.valueOf(feedback.getUnderstandabilityScore()) ) / (nVotes+1)));
				newQuestion.setAverageGradeDifficulty(String.valueOf( ( (nVotes*Double.valueOf(newQuestion.getAverageGradeDifficulty())) +  Double.valueOf(feedback.getDifficultyScore()) ) / (nVotes+1)));
				newQuestion.setAverageGradeTime(String.valueOf( ( (nVotes*Double.valueOf(newQuestion.getAverageGradeTime())) +  Double.valueOf(feedback.getTimeScore()) ) / (nVotes+1)));
				newQuestion.setAverageGrade(String.valueOf( (  Double.valueOf(newQuestion.getAverageGradeUnderstability())+Double.valueOf(newQuestion.getAverageGradeDifficulty()) + Double.valueOf(newQuestion.getAverageGradeTime())  ) / 3));
			}else {
				newQuestion.setNumberVotes("1");
				newQuestion.setAverageGradeUnderstability(String.valueOf(Double.valueOf(feedback.getUnderstandabilityScore())));
				newQuestion.setAverageGradeDifficulty(String.valueOf(Double.valueOf(feedback.getDifficultyScore()) ));
				newQuestion.setAverageGradeTime(String.valueOf(Double.valueOf(feedback.getTimeScore())));
				newQuestion.setAverageGrade(String.valueOf( (  Double.valueOf(feedback.getUnderstandabilityScore())+Double.valueOf(feedback.getDifficultyScore()) + Double.valueOf(feedback.getTimeScore())  ) / 3));
			}
		Question q =questionRepository.save(newQuestion);

		return ResponseEntity.status(HttpStatus.OK).body(q.toString());
	}

}
