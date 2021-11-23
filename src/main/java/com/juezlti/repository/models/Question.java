package com.juezlti.repository.models;


import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;


@Data
@Document(collection = "test")
public class Question {

	@Id
	private String id;

	private String title;
	private String module;
	private String owner_id;
	private String project_id;
	private List<String> keywords;
	private String event;
	private String platform;
	private String status;
	private Number timeout;
	private List<String> programmingLanguages;
	private Date updated_at;
	private Date created_at;
	
	private String type;
	private String difficulty;
	private String averageGradeUnderstability;
	private String averageGradeDifficulty;
	private String averageGradeTime;
	private String averageGrade;
	private String numberVotes;
	private String question_solution;
	
	private String question_must;
	private String question_musnt;

	// SQL
	private Integer question_dbms;
	private String question_sql_type;
	private String question_database;
	private String question_probe;
	private String question_onfly;
	
	//Code
	private Integer question_language;
	private String question_input_test;
	private String question_input_grade;
	private String question_output_test;
	private String question_output_grade;
	private String recalculateOutputs;
	
	@Transient
	@JsonProperty("isCodeQuestion")
	private boolean codeQuestion;
	@Transient
	@JsonProperty("isSqlQuestion")
	private boolean sqlQuestion;


}
