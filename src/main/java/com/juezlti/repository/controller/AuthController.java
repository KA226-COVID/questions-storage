package com.juezlti.repository.controller;


import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import com.juezlti.repository.config.UserDetailsImpl;
import com.juezlti.repository.util.JwtUtils;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;




	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;
	
	@RequestMapping(value = "/signin", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> authenticateUser1() {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken("prueba", "pass"));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		return ResponseEntity.ok(jwt);
	}

	@RequestMapping(value = "/me", method = {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<?> userInfo(Authentication authentication){
		UserDetailsImpl userDetails = Optional.ofNullable(authentication.getPrincipal())
				.filter(UserDetailsImpl.class::isInstance)
				.map(UserDetailsImpl.class::cast)
				.orElse(null);
		if(userDetails == null){
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(userDetails.getUsername());
	}
	
}

