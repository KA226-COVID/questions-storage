package com.juezlti.repository.models;

import lombok.Data;

@Data
public class Statements {
	
	private String id;
	private String pathname;
	private String nat_lang;
	private String format;
	
	private String exercise_id;
}
