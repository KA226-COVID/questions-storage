package com.juezlti.repository.storage;

import lombok.Data;

@Data
public class FileData {

    private String filename;
    private String url;
    private Long size;
    private String sizeTxt;

}
