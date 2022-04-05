package com.juezlti.repository.models.yapexil;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.juezlti.repository.models.Exercise;

@Data
@NoArgsConstructor
public class ExerciseMetadata {
    String id;
    String title; 
    String module;
    String owner;
    String difficulty;
    String status;

    public ExerciseMetadata(Exercise exercise){
        this.id = exercise.getId();
        this.title = exercise.getTitle();
        this.module = exercise.getModule();
        this.owner  = exercise.getOwner_id();
        this.difficulty = exercise.getDifficulty();
        this.status = exercise.getStatus();
    }

}
