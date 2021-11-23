package com.juezlti.repository.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.juezlti.repository.models.UserAuth;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
//	@Autowired
//	UserRepository userRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserAuth user = new UserAuth();
		if(username.equals("prueba")) {
		
			user.setPassword("$2a$10$zu3MSY67B21R2sgkMrQl6eARELhgrrnrVf0BWvU6mLNHOallqdh.e");
			user.setUsername("prueba");
		}
		
		return UserDetailsImpl.build(user);
	}

}