package com.juezlti.repository.models;

import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Test {

	@Id
	private String id;

	private String name; //statement
	private String description;
	private String status;
	private boolean is_public;
//	private String statement;
	private List<Question> questions;
	
	public List<Question> getQuestions() {
		return questions;
	}
	
	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}
	
	
}
