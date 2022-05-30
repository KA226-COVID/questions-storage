package com.juezlti.repository.service;

import com.juezlti.repository.controller.ExerciseController;
import com.juezlti.repository.storage.FilesController;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
public class ExerciseService {

    @Value("${files-storage.basepath:/codetest}")
    private String baseUploadStrPath;

    @Value("${files-storage.upload:/upload}")
    private String uploadStrPath;

    @Value("${files-storage.exercises:/exercises}")
    private String exercisesStrPath;

    public static final String SOLUTIONS_FOLDER  = "solutions";
    public static final String STATEMENTS_FOLDER = "statements";
    public static final String TESTS_FOLDER      = "tests";
    public static final String LIBRARIES_FOLDER  = "libraries";

    public List<String> getExerciseFiles(String id, String folder){
        Path root = Paths.get(baseUploadStrPath, exercisesStrPath, id, folder);
        if (!Files.exists(root)) {
            return null;
        }
        try {
            return Files.walk(root, 2)
                    .filter(path -> !Files.isDirectory(path) && !"metadata.json".equals(path.getFileName().toString()))
                    .map(el -> {
                        try {
                            String methodName = "";
                            switch (folder){
                                case STATEMENTS_FOLDER:
                                default:
                                    methodName = "getAuthorkitExerciseStatement";
                                    break;
                                case TESTS_FOLDER:
                                    methodName = "getAuthorkitExerciseTests";
                                    break;
                                case SOLUTIONS_FOLDER:
                                    methodName = "getAuthorkitExerciseSolution";
                                    break;
                                case LIBRARIES_FOLDER:
                                    methodName = "getAuthorkitExerciseLibraries";
                                    break;
                            }

                            String filePath = el.getParent().getFileName().toString();
                            String filenameEncoded = URLEncoder.encode(el.getFileName().toString(), StandardCharsets.UTF_8.toString());
                            String value = MvcUriComponentsBuilder
                                    .fromMethodName(
                                            ExerciseController.class,
                                            methodName,
                                            id,
                                            filePath + "/" + filenameEncoded
                                    )
                                    .build()
                                    .toString()
                                    .replace("**", filePath + "/" + filenameEncoded);
                            return value;
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public List<String> getExerciseStatements(String id){
        return getExerciseFiles(id, STATEMENTS_FOLDER);
    }

    public List<String> getExerciseTests(String id){
        return getExerciseFiles(id, TESTS_FOLDER);
    }

    public List<String> getExerciseSolutions(String id){
        return getExerciseFiles(id, SOLUTIONS_FOLDER);
    }

    public List<String> getExerciseLibraries(String id){
        return getExerciseFiles(id, LIBRARIES_FOLDER);
    }

}
