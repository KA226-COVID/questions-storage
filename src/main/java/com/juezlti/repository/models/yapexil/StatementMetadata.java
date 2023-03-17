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

    public StatementMetadata(Exercise exercise) {
        this.id = UUID.randomUUID().toString();
        this.nat_lang = exercise.getSessionLanguage();
        this.pathname = "statement." + this.format.toLowerCase();
        this.exerciseId = exercise.getAkId();
    }

    public String getStatementStringPath() {
        return this.getId() + "/" + this.getPathname();
    }

    public Path getStatementPath(){
        return Paths.get(this.getFileStringPath());
    }

    public String getFileStringPath() {
        return this.getExerciseId() + "/" + STATEMENTS_FOLDER + "/" + this.getId() + "/" + this.getPathname();
    }

    public Path getFilePath() {
        return Paths.get(this.getFileStringPath());
    }

    public boolean checkLanguage(String lang) {

        String nat_lang = this.nat_lang;
        String nat_lang2 = lang;

        if ( nat_lang.replace('-','_').toLowerCase().equals(nat_lang2.replace('-','_').toLowerCase()) ) {
            return true;
        } else {
            String string1 = nat_lang.replace('-','_').toLowerCase();
            String string2 =nat_lang2.replace('-','_').toLowerCase();
            String[] parts = string1.split("_");
            String[] parts2 = string2.split("_");
            String part1 = parts[0];
            String part2 = parts2[0];

            if (part1.equals(part2)) {
                return true;
            }

            return false;
        }

    }

}
