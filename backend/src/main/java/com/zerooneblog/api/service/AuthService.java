package com.zerooneblog.api.service;

import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.domain.Role;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.security.JwtTokenProvider;
import com.zerooneblog.api.interfaces.dto.requestDto.UserLoginRequest;
import com.zerooneblog.api.interfaces.dto.requestDto.UserRegistrationRequest;
import com.zerooneblog.api.interfaces.exception.DuplicateResourceException;
import java.util.Set;


import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String authenticateUser(UserLoginRequest userLoginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequest.getUsername(),
                        userLoginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);
    }

    public User registerUser(UserRegistrationRequest registrationRequest) {
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new DuplicateResourceException("User", "username", registrationRequest.getUsername());
        }
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new DuplicateResourceException("User", "email", registrationRequest.getEmail());
        }

        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());
        user.setName(registrationRequest.getName());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setRoles(Set.of(Role.USER));

        return userRepository.save(user);
    }
}