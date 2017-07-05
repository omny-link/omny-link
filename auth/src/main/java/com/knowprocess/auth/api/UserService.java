package com.knowprocess.auth.api;

import java.util.Optional;

import com.knowprocess.auth.user.model.User;

/**
 *
 * @author vladimir.stankovic
 *
 * Aug 17, 2016
 */
public interface UserService {
    public Optional<User> getByUsername(String username);
}
