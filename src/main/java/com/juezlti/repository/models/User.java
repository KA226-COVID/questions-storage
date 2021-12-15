package com.juezlti.repository.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@NoArgsConstructor
@Document(collection = "user")
public class User {
    
    @Id
	private String id;
	
    @JsonProperty("displayname")
    private String displayName;
    
    @JsonProperty("username")
    private String userName;
    
    private String email;
    
    private String password;

    public User(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }
}
