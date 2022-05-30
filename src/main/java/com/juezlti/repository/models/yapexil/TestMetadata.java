package com.juezlti.repository.models.yapexil;

import com.juezlti.repository.models.Exercise;

import com.google.gson.annotations.Expose;

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
    @Expose(serialize = true)
    String id;    
    @Expose(serialize = true)
    Number weight;
    @Expose(serialize = true)
    Boolean visible;
    @Expose(serialize = true)
    String input;
    @Expose(serialize = true)
    String output;      
    @Expose(serialize = true)
    List<String> arguments; 
    @Expose(serialize = true)
    String inputValue;
    @Expose(serialize = true)
    String outputValue;
    @Expose(serialize = true)
    Object feedback;
    @Expose(serialize = false)
    private String exerciseId;

    public TestMetadata(Exercise exercise){
        this.id = UUID.randomUUID().toString();
        this.exerciseId = exercise.getAkId();
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
