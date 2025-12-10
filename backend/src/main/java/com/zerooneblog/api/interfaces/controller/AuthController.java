package com.zerooneblog.api.interfaces.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.zerooneblog.api.service.AuthService;

import com.zerooneblog.api.domain.User;
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
    public String LoginUser(@Valid @RequestBody UserLoginRequest signupRequest) {
        String token = authService.authenticateUser(signupRequest);
        return "User " + signupRequest.getUsername() +  "logged, token: " + token;
    }

    @PostMapping("/signup")
    public String RegisterUser(@Valid @RequestBody UserRegistrationRequest signinRequest) {
        User savedUser = authService.registerUser(signinRequest);
        return "User Registered: " + savedUser.getUsername();
    }
}