package com.juezlti.repository.models.yapexil;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import com.juezlti.repository.models.Exercise;

@Data
@NoArgsConstructor
public class ExerciseMetadata {
    String id;
    String title; 
    String module;
    String owner;
    String author;
    String difficulty;
    String status;
    String BASE_URL = "";
    String EMAIL = "";
    String PASSWORD = "";
    List<String> keywords;
    String type = "BLANK_SHEET";
    String event;
    String platform;
    Number timeout;
    List<String> programmingLanguages;
    String created_at;
    String updated_at;

    //Si es necesario añadir Los demas metadatas para completar toda la informacion del Exercise
    public ExerciseMetadata(Exercise exercise){
        this.id = exercise.getAkId();
        this.title = exercise.getTitle();
        this.module = (exercise.getModule() == null)? "" : exercise.getModule();
        this.owner  = exercise.getOwner();
        this.difficulty = exercise.getDifficulty();
        this.status = (exercise.getStatus() == null)? "PUBLISHED" : exercise.getStatus();
        this.keywords = exercise.getKeywords();
        this.event = (exercise.getEvent() == null)? "" : exercise.getEvent();
        this.platform = (exercise.getPlatform() == null)? "" : exercise.getPlatform();
        this.timeout = (exercise.getTimeout() == null)? 0 : exercise.getTimeout();       
        this.created_at = exercise.getCreated_at().toString();
        this.updated_at = (exercise.getUpdated_at() == null)? exercise.getCreated_at().toString() : exercise.getUpdated_at().toString();
        this.author = exercise.getAuthor();

        if(exercise.getProgrammingLanguages() == null){
            List<String> list = new ArrayList<String>();
            list.add(exercise.getExercise_language().toLowerCase());
            this.programmingLanguages = list;
        }else{
            this.programmingLanguages = exercise.getProgrammingLanguages(); 
        }

    }

}
