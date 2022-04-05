package com.juezlti.repository.repository;

import com.juezlti.repository.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

	User findByUserName(String userName);

}
