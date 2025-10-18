    package com.zerooneblog.api.infrastructure.controller;

    import org.springframework.web.bind.annotation.*;

import com.zerooneblog.api.interfaces.dto.UserRegistrationRequest;
import com.zerooneblog.api.interfaces.dto.UserLoginRequest;


    @RestController
    @RequestMapping("/api/auth")
    public class AuthController {

        @PostMapping("/signin")
        public String LoginUser(@RequestBody UserLoginRequest signupRequest) {
            System.out.println("Registering user: " + signupRequest.getUsername());
            return "User Registered: " + signupRequest.getUsername(); 
        }


        @PostMapping("/signup")
        public String RegisterUser(@RequestBody UserRegistrationRequest signinRequest) {
            System.out.println("Registering user: " + signinRequest.GetUsername());
            return "User Registered: " + signinRequest.GetUsername();
        }
    }