package com.juezlti.repository.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.juezlti.repository.models.Feedback;
import com.juezlti.repository.models.Test;



public interface FeedbackRepository extends MongoRepository<Feedback, String> {
	
	
	 @Query( value = "{idQuestion: {$in : ?0}, user:{userId :{$in : ?1 } } } ")
	  List<Feedback> findByIds ( List<String> list,  List<String> list2);
	  
	 List<Feedback> findByIdQuestionInAndUserIdInAndCtId(List<String> idQuestions, List<String> idUser, String ctId);
	 
	 int countByCtId(String ctId);
	
	  List<Feedback> findByIdQuestionIgnoreCase(String idQuestion); 
	  List<Feedback> findByDate(Date date); 
	}
