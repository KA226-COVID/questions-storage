package com.juezlti.repository.models.yapexil;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.juezlti.repository.service.ExerciseService.TESTS_FOLDER;

@Data
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
