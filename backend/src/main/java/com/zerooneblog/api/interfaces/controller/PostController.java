package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.interfaces.dto.*;
import com.zerooneblog.api.service.PostService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("title") @Size(max = 255, message = "Title must not exceed 255 characters") String title,
            @RequestParam("content") @Size(max = 1500, message = "Content must not exceed 1500 characters") String content,
            @RequestParam(value = "mediaFiles", required = false) MultipartFile[] mediaFiles,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        PostDTO postDto = new PostDTO(title, content, mediaFiles);

        PostResponse createdPost = postService.createPost(postDto, username);
        return ResponseEntity.ok(createdPost);
    }

    @GetMapping
    public ResponseEntity<PostsResponseDto> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        PostsResponseDto posts = postService.getAllPosts(page, size, authentication);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/feed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostsResponseDto> getFeedForCurrentUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        PostsResponseDto posts = postService.getFeedForCurrentUser(page, size, authentication);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id, Authentication authentication) {
        PostResponse post = postService.getPostById(id, authentication);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> updatePost(
            @Valid @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "mediaFiles", required = false) MultipartFile[] mediaFiles,
            Authentication authentication) {
        PostResponse updatedPost = postService.updatePost(id, title, content, mediaFiles, authentication);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deletePost(@PathVariable Long id,
            Authentication authentication) {
        String message = postService.deletePost(id, authentication);
        MessageResponse messageResponse = new MessageResponse("SUCCESS", message);
        return ResponseEntity.ok(messageResponse);
    }

}
