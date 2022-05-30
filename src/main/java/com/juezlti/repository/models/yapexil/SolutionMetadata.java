package com.juezlti.repository.models.yapexil;

import com.google.gson.annotations.Expose;

import com.juezlti.repository.models.Exercise;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Data
@NoArgsConstructor
public class SolutionMetadata {
    @Expose(serialize = true)
    String id;
    @Expose(serialize = true)
    String pathname; 
    @Expose(serialize = true)
    String lang;
    @Expose(serialize = false)
    String exerciseId;

    public SolutionMetadata(Exercise exercise){
        this.exerciseId = exercise.getAkId();
        this.id = UUID.randomUUID().toString();
        this.lang  = exercise.getExercise_language();
        this.pathname = "solution." + exercise.getExercise_language().toLowerCase();
    }

    public String getSolutionStringPath(){
        return this.getId() + "/" + this.getPathname();
    }
    
    public Path getSolutionPath(){
        return Paths.get(this.getSolutionStringPath());
    }



}
