package com.juezlti.repository.models;

import java.util.Date;
import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;


@Data
public class Feedback {

	@Id
	private String id;
	private String idQuestion;
	private String ctId;
	private LocalDate date;
	private User user;
	private Integer understandabilityScore;
	private Integer difficultyScore;
	private Integer timeScore;

}
