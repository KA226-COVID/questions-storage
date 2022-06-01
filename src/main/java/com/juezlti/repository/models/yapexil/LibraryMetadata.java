package com.juezlti.repository.models.yapexil;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.json.JSONObject;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import com.juezlti.repository.models.Exercise;
import static com.juezlti.repository.service.ExerciseService.LIBRARIES_FOLDER;

@Data
@NoArgsConstructor
public class LibraryMetadata {
    String id;
    MultipartFile library;
    String pathname;
    String type;
    private String exerciseId;

    public LibraryMetadata(Exercise exercise, MultipartFile library){
        this.id = UUID.randomUUID().toString();
        this.exerciseId = exercise.getAkId();
        this.library = library;
        this.pathname = library.getOriginalFilename();
        this.type = "LIBRARY";
    }

    public String getLibraryStringPath(){
        return this.getId() + "/" + this.getPathname();
    }

    public Path getLibraryPath(){
        return Paths.get(this.getFileStringPath());
    }

    public String getFileStringPath(){
        return this.getExerciseId() + "/" + LIBRARIES_FOLDER + "/" + this.getId() + "/";
    }

    public Path getFilePath(){
        return Paths.get(this.getFileStringPath());
    }
}
