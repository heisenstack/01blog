    package com.zerooneblog.api.infrastructure.controller;

    import org.springframework.web.bind.annotation.*;

import com.zerooneblog.api.interfaces.dto.UserRegistrationRequest;

    @RestController
    @RequestMapping("/api/auth")
    public class AuthController {

        @PostMapping("/signin")
        public String LoginUser(@ResponseBody UserLoginRequest request) {
            System.out.println("Registering user: " + request.username);
            return "User Registered: " + request.username; 
        }


        @PostMapping("/signup")
        public String RegisterUser(@RequestBody UserRegistrationRequest request) {
            System.out.println("Registering user: " + request.username);
            return "User Registered: " + request.username;
        }
    }