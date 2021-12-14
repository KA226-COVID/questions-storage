package com.juezlti.repository.models;


import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@NoArgsConstructor
@Document(collection = "user")
public class User {
    
	private String id;
    private String displayName;
    private String userName;
    private String email;
    private String password;

}
