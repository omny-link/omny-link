package com.knowprocess.auth.user.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.knowprocess.auth.api.UserService;
import com.knowprocess.auth.user.model.User;
import com.knowprocess.auth.user.repositories.UserRepository;


/**
 */
@Service
public class DatabaseUserService implements UserService {
    private final UserRepository userRepo;

    @Autowired
    public DatabaseUserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public UserRepository getUserRepository() {
        return userRepo;
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return userRepo.findByUsername(username);
    }
}
