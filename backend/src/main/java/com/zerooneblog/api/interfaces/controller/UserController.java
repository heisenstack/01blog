package com.zerooneblog.api.interfaces.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.zerooneblog.api.interfaces.dto.MessageResponse;
import com.zerooneblog.api.interfaces.dto.UserProfileDto;
import com.zerooneblog.api.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(username, page, size, authentication));
    }

    @PostMapping("/{userId}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> followUser(@PathVariable Long userId, Authentication authentication) {
        String followMessaString = userService.followUser(userId, authentication);
        MessageResponse message = new MessageResponse("SUCCESS", followMessaString);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{userId}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> unfollowUser(@PathVariable Long userId, Authentication authentication) {
        String unfollowMessage = userService.unfollowUser(userId, authentication);
        MessageResponse message = new MessageResponse("SUCCESS", unfollowMessage);
        return ResponseEntity.ok(message);
    }
}
