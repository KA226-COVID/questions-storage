package com.juezlti.repository.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
public class User {
	
//	@Id
	@Field("userId")
	private String id;
    private String profile_id;
    private String displayname;
    private String email;

	
	public User() {
	}


}
