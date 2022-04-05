package com.juezlti.repository.models.yapexil;

import com.juezlti.repository.models.Exercise;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@NoArgsConstructor
public class SolutionMetadata {
    String id;
    String exerciseId;
    String pathname; 
    String lang;

    public SolutionMetadata(Exercise exercise){
        this.id = exercise.generateFoldersId();
        this.exerciseId = exercise.getId();
        this.lang  = exercise.getExercise_language();
        this.pathname = "solution.";             
    }

    public String getSolutionStringPath(){
        return this.getId() + "/" + this.getPathname()+this.lang;
    }
    
    public Path getSolutionPath(){
        return Paths.get(this.getSolutionStringPath());
    }



}
