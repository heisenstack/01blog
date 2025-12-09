package com.zerooneblog.api.service;

import org.springframework.stereotype.Service;

import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }
}
