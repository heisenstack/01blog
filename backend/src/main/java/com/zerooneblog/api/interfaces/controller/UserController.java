package com.zerooneblog.api.interfaces.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.zerooneblog.api.interfaces.dto.*;
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

    @PostMapping("/{username}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> followUser(@PathVariable String username, Authentication authentication) {
        String followMessaString = userService.followUser(username, authentication);
        MessageResponse message = new MessageResponse("SUCCESS", followMessaString);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{username}/unfollow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> unfollowUser(@PathVariable String username, Authentication authentication) {
        String unfollowMessage = userService.unfollowUser(username, authentication);
        MessageResponse message = new MessageResponse("SUCCESS", unfollowMessage);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/suggestions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSuggestionResponse> getSuggestedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserSuggestionResponse suggestions = userService.getSuggestedUsers(page, size);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/following")
    public ResponseEntity<UserSuggestionResponse> getFollowingUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserSuggestionResponse following = userService.getFollowingUsers(page, size);
        return ResponseEntity.ok(following);
    }
}