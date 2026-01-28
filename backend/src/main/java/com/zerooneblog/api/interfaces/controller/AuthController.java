package com.zerooneblog.api.interfaces.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


import com.zerooneblog.api.service.AuthService;

import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.interfaces.dto.JwtAuthenticationResponse;
import com.zerooneblog.api.interfaces.dto.MessageResponse;

import com.zerooneblog.api.interfaces.dto.requestDto.UserLoginRequest;
import com.zerooneblog.api.interfaces.dto.requestDto.UserRegistrationRequest;

// Authentication endpoints for user login and registration
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Authenticate user and return JWT token
    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> LoginUser(@Valid @RequestBody UserLoginRequest signupRequest) {
        String token = authService.authenticateUser(signupRequest);
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }

    // Register new user
    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> RegisterUser(@Valid @RequestBody UserRegistrationRequest signinRequest) {
        User savedUser = authService.registerUser(signinRequest);
        return ResponseEntity.ok(
                new MessageResponse("SUCCESS", "User: " + savedUser.getName() + " has been registered successfully."));
    }

    // Verify if JWT token is valid
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            return ResponseEntity.ok(new MessageResponse("SUCCESS", "Token is valid"));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("FAILURE", "Invalid token"));
    }
    
}