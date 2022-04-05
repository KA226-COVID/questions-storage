package com.juezlti.repository.models.yapexil;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.juezlti.repository.models.Exercise;
import static com.juezlti.repository.service.ExerciseService.STATEMENTS_FOLDER;

@Data
@NoArgsConstructor
public class StatementMetadata {
    String id;
    String exerciseId; 
    String pathname;
    String nat_lang;
    String format;

    public StatementMetadata(Exercise exercise){
        this.id = exercise.generateFoldersId();
        this.exerciseId = exercise.getId();
        this.nat_lang = exercise.getSession_language();
        this.format = "html";
        this.pathname = "statement.";
    }
    
    public String getStatementStringPath(){
        return this.getId() + "/" + this.getPathname()+this.pathname+this.format;
    }
    
    public Path getStatementPath(){
        return Paths.get(this.getFileStringPath());
    }

    public String getFileStringPath(){
        return this.getExerciseId() + "/" + STATEMENTS_FOLDER + "/" + this.getId() + "/" + this.getPathname();
    }
    
    public Path getFilePath(){
        return Paths.get(this.getFileStringPath());
    }
}
