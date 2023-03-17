package com.juezlti.repository.models;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@Document(collection = "test")
public class Exercise {

	@Id
	private String id;
	private String akId;
	private String title;
	private String module;
	private String owner_id;
	private String owner;
	private String author;
	private String project_id;
	private List<String> keywords;
	private String event;
	private String platform;
	private String status;
	private Number timeout;
	private List<String> programmingLanguages;
	private LocalDateTime updated_at;
	private LocalDateTime created_at;
	private String statement;
	private String hint;
	private String difficulty;
	private String averageGradeUnderstability;
	private String averageGradeDifficulty;
	private String averageGradeTime;
	private String averageGrade;
	private String numberVotes;
	private String exercise_solution;
	private String sessionLanguage = "en";
	
	@Transient
	@JsonIgnore
	private List<MultipartFile> exercise_libraries = null;

	//Code
	private String exercise_language;
	private Map<String, String> exercise_input_test;
	private Map<String, String> exercise_output_test;
	private Map<String, Boolean> visibleTest;

	private boolean codeExercise;
	
	public Exercise(String akId, String sessionLanguage) {
		this.akId = akId;
		this.sessionLanguage = sessionLanguage;
	}

}
