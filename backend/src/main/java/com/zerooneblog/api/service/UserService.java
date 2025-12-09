package com.zerooneblog.api.service;

import org.springframework.security.core.Authentication;
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

    public User findById(Long userId) {
        return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
 
    }

    public User getCurrentUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())){
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}
