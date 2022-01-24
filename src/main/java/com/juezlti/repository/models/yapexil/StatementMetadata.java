package com.juezlti.repository.models.yapexil;

import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.juezlti.repository.service.ExerciseService.STATEMENTS_FOLDER;

@Data
public class StatementMetadata {
    String id;
    String exerciseId; 
    String pathname;
    String nat_lang;
    String format;
    
    public String getFileStringPath(){
        return this.getExerciseId() + "/" + STATEMENTS_FOLDER + "/" + this.getId() + "/" + this.getPathname();
    }
    
    public Path getFilePath(){
        return Paths.get(this.getFileStringPath());
    }
}
