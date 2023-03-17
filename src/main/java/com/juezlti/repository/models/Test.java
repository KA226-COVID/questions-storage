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
	private List<Exercise> exercises;
	
	public List<Exercise> getExercises() {
		return exercises;
	}
	
	public void setExercises(List<Exercise> exercises) {
		this.exercises = exercises;
	}
	
	
}
