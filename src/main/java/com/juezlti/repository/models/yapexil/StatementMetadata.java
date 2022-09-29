package com.juezlti.repository.models.yapexil;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.juezlti.repository.models.Exercise;
import static com.juezlti.repository.service.ExerciseService.STATEMENTS_FOLDER;

@Data
@NoArgsConstructor
public class StatementMetadata {
    String id;
    String pathname;
    String nat_lang;
    String statementValue;
    String format = "HTML";
    private String exerciseId;

    public StatementMetadata(Exercise exercise){
        this.id = UUID.randomUUID().toString();
        this.nat_lang = exercise.getSessionLanguage();
        this.pathname = "statement." + this.format.toLowerCase();
        this.exerciseId = exercise.getAkId();
    }

    public String getStatementStringPath(){
        return this.getId() + "/" + this.getPathname();
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
