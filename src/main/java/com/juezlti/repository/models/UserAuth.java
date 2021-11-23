package com.juezlti.repository.models;

//import javax.persistence.Entity;
//import javax.persistence.Id;

import lombok.Data;

//@Entity
@Data
public class UserAuth {
//	@Id
	private Long id;
	private String username;
	private String email;
	private String password;

	
	public UserAuth() {
	}

	public UserAuth(String username,  String password) {
		this.username = username;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}