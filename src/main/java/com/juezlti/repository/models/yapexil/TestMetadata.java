package com.juezlti.repository.models.yapexil;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.juezlti.repository.models.Exercise;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static com.juezlti.repository.service.ExerciseService.TESTS_FOLDER;

@Data
@NoArgsConstructor
public class TestMetadata {
    String id;    
    Number weight;
    Boolean visible;
    String input;
    String output;      
    List<String> arguments; 
    String inputValue;
    String outputValue;
    Object feedback;
    private String akId;

    public TestMetadata(Exercise exercise){
        this.id = UUID.randomUUID().toString();
        this.weight = 0;
        this.visible = true;
        this.input = "input.txt";
        this.output = "output.txt";
        this.arguments = exercise.getKeywords();
        this.feedback = new Object();
    }

    public String calcInputValue(String base) {
        return Paths.get(
                base,
                this.getAkId(),
                TESTS_FOLDER,
                id,
                this.getInput()
        ).toString();
    }

    public String calcOutputValue(String base) {
        return Paths.get(
                base,
                this.getAkId(),
                TESTS_FOLDER,
                id,
                this.getOutput()
        ).toString();
    }
    
}
