package com.juezlti.repository.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user")
public class User {

    @Id
	private String id;

    @JsonProperty("displayname")
    private String displayName;

    @JsonProperty("username")
    @Indexed(unique=true)
    private String userName;

    @JsonProperty("profile_id")
    private String profileId;

    @JsonProperty("email")
    private String email;

    @JsonProperty( value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
