package com.zerooneblog.api.interfaces.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.zerooneblog.api.interfaces.dto.UserProfileDto;
import com.zerooneblog.api.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId,  Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(userId, authentication));
    }
    

}
