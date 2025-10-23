package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.interfaces.dto.PostDTO;
import com.zerooneblog.api.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<Post> createPost(@RequestBody PostDTO request, 
                                           @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        
        Post createdPost = postService.createPost(request, username);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }
}
