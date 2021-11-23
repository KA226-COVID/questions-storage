package com.juezlti.repository.models;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Author {

	@Id
	private String id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String phone;
	private int userStatus;

}
