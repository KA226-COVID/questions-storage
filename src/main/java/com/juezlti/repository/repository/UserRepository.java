package com.juezlti.repository.repository;

import com.juezlti.repository.models.Usage;
import com.juezlti.repository.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;


public interface UserRepository extends MongoRepository<User, String> {

	User findByUserName(String userName);

}
