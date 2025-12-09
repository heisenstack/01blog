package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.interfaces.dto.PostDTO;
import com.zerooneblog.api.interfaces.dto.PostResponse;
import com.zerooneblog.api.interfaces.dto.PostAuthorResponse;
import com.zerooneblog.api.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        PostResponse createdPost = postService.createPost(request, username);
        return  ResponseEntity.ok(createdPost);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts(Authentication authentication) {
        List<PostResponse> posts = postService.getAllPosts(authentication);

        return ResponseEntity.ok(posts);
    }

    // @GetMapping("/{id}")
    // public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
    //     Post post = postService.getPostById(id);
    //     PostResponse postResponse = mapToPostResponse(post);
    //     return ResponseEntity.ok(postResponse);
    // }

    // @PutMapping("/{id}")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody PostDTO request, @AuthenticationPrincipal UserDetails userDetails) {
    //     String username = userDetails.getUsername();
    //     Post updatedPost = postService.updatePost(id, request, username);
    //     PostResponse postResponse = mapToPostResponse(updatedPost);
    //     return ResponseEntity.ok(postResponse);
    // }

    // @DeleteMapping("/{id}")
    // public ResponseEntity<String> deletePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
    //     String username = userDetails.getUsername();
    //     String message = postService.deletePost(id, username);
    //     return ResponseEntity.ok(message);
    // }

}
