package com.juezlti.repository.models;

import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Set {

	@Id
	private String id;
	private String name;
	private String  description;
	private Author owner;
	private String status;
	private List<Question> questions;
	
	
}
