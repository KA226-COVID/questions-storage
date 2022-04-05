package com.juezlti.repository.models.yapexil;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.juezlti.repository.models.Exercise;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.juezlti.repository.service.ExerciseService.TESTS_FOLDER;

@Data
@NoArgsConstructor
public class TestMetadata {
    String id;
    String exerciseId;    
    String weight;
    Boolean visible;
    String input;
    String output;   
    Number timeout;   
    List<String> arguments; 
    String inputValue;
    String outputValue;

    public TestMetadata(Exercise exercise){
        this.id = exercise.generateFoldersId();
        this.exerciseId = exercise.getId();
        this.weight = "1";
        this.visible = true;
        this.input = "input.txt";
        this.output = "output.txt";
        this.timeout = exercise.getTimeout();
        this.arguments = exercise.getKeywords();
        this.inputValue = exercise.getExercise_input_test();
        this.outputValue = exercise.getExercise_output_test();

    }

    public String calcInputValue(String base) {
        return Paths.get(
                base,
                this.getExerciseId(),
                TESTS_FOLDER,
                id,
                this.getInput()
        ).toString();
    }

    public String calcOutputValue(String base) {
        return Paths.get(
                base,
                this.getExerciseId(),
                TESTS_FOLDER,
                id,
                this.getOutput()
        ).toString();
    }
    
}
