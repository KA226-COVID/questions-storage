package com.juezlti.repository.models;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import lombok.Data;
@Data
public class Usage {

	@Id
	private String id;
	private String idExercise;
	private String ctId;
	private LocalDate date;
	private User user;
	private Integer understandabilityScore;
	private Integer difficultyScore;
	private Integer timeScore;

}
