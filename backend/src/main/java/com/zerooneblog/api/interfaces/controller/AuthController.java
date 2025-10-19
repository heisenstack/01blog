package com.zerooneblog.api.interfaces.controller;

// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zerooneblog.api.interfaces.dto.UserRegistrationRequest;
import com.zerooneblog.api.interfaces.dto.UserLoginRequest;
import com.zerooneblog.api.service.AuthService;

import com.zerooneblog.api.domain.User;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    // @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/signin")
    public String LoginUser(@RequestBody UserLoginRequest signupRequest) {
        String token = authService.authenticateUser(signupRequest);
        return "User " + signupRequest.getUsername() +  "logged, token: " + token;
    }

    @PostMapping("/signup")
    public String RegisterUser(@RequestBody UserRegistrationRequest signinRequest) {
        User savedUser = authService.registerUser(signinRequest);
        return "User Registered: " + savedUser.getUsername();
    }
}