package com.juezlti.repository.auth;

import java.util.ArrayList;
import java.util.Optional;

import com.juezlti.repository.models.User;
import com.juezlti.repository.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsersAuthService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder bcryptEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUserName(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
		return new org.springframework.security.core.userdetails.User(
				user.getUserName(),
				user.getPassword(),
				new ArrayList<>()
		);
	}
	
	public User createUser(User user) {
		user.setDisplayName(
				Optional.ofNullable(user.getDisplayName())
						.orElse(user.getUserName().toLowerCase())
		);
		user.setPassword(bcryptEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}
}