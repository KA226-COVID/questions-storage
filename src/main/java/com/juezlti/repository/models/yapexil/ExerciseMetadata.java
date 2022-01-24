package com.juezlti.repository.models.yapexil;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.juezlti.repository.service.ExerciseService.STATEMENTS_FOLDER;

@Data
public class ExerciseMetadata {
    String id;
    String title; 
    String module;
    String owner;
    String type;
    String difficulty;
    String status;
}
