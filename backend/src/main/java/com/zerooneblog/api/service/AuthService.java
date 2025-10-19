package com.zerooneblog.api.service;

import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.dto.UserRegistrationRequest;
import com.zerooneblog.api.domain.User;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(UserRegistrationRequest registrationRequest) {
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }
        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(registrationRequest.getPassword());

        return userRepository.save(user);
    }
}
