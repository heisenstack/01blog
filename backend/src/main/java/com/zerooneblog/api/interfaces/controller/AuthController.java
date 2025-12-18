package com.zerooneblog.api.interfaces.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zerooneblog.api.service.AuthService;

import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.interfaces.dto.JwtAuthenticationResponse;
import com.zerooneblog.api.interfaces.dto.MessageResponse;

import com.zerooneblog.api.interfaces.dto.requestDto.UserLoginRequest;
import com.zerooneblog.api.interfaces.dto.requestDto.UserRegistrationRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtAuthenticationResponse> LoginUser(@Valid @RequestBody UserLoginRequest signupRequest) {
        // String token = authService.authenticateUser(loginRequest);
        String token = authService.authenticateUser(signupRequest);
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
        // return token;
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> RegisterUser(@Valid @RequestBody UserRegistrationRequest signinRequest) {
        User savedUser = authService.registerUser(signinRequest);
        return ResponseEntity.ok(
                new MessageResponse("SUCCESS", "User: " + savedUser.getName() + " has been registered successfully."));
    }
}