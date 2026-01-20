package com.zerooneblog.api.interfaces.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.zerooneblog.api.interfaces.dto.*;
import com.zerooneblog.api.service.UserService;

// Endpoints for managing user profiles and relationships
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get user profile with their posts
    @GetMapping("/{username}")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(username, page, size, authentication));
    }

    // Follow a user
    @PostMapping("/{username}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> followUser(@PathVariable String username, Authentication authentication) {
        String followMessaString = userService.followUser(username, authentication);
        MessageResponse message = new MessageResponse("SUCCESS", followMessaString);
        return ResponseEntity.ok(message);
    }

    // Unfollow a user
    @DeleteMapping("/{username}/unfollow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> unfollowUser(@PathVariable String username, Authentication authentication) {
        String unfollowMessage = userService.unfollowUser(username, authentication);
        MessageResponse message = new MessageResponse("SUCCESS", unfollowMessage);
        return ResponseEntity.ok(message);
    }

    // Get suggested users to follow
    @GetMapping("/suggestions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSuggestionResponse> getSuggestedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserSuggestionResponse suggestions = userService.getSuggestedUsers(page, size);
        return ResponseEntity.ok(suggestions);
    }

    // Get list of users current user is following
    @GetMapping("/following")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<UserSuggestionResponse> getFollowingUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserSuggestionResponse following = userService.getFollowingUsers(page, size);
        return ResponseEntity.ok(following);
    }
}