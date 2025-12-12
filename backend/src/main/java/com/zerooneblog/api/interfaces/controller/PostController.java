package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.interfaces.dto.MessageResponse;
import com.zerooneblog.api.interfaces.dto.PostDTO;
import com.zerooneblog.api.interfaces.dto.PostResponse;
import com.zerooneblog.api.interfaces.dto.PostsResponseDto;
import com.zerooneblog.api.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "mediaFiles", required = false) MultipartFile[] mediaFiles,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        PostDTO postDto = new PostDTO(title, content, mediaFiles);

        PostResponse createdPost = postService.createPost(postDto, username);
        return ResponseEntity.ok(createdPost);
    }

    @GetMapping
    public ResponseEntity<PostsResponseDto> getAllPosts(
        @RequestParam(defaultValue =  "0") int page,
        @RequestParam(defaultValue = "10") int size,
        Authentication authentication) {
        PostsResponseDto posts = postService.getAllPosts(page, size,authentication);

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
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "mediaFiles", required = false) MultipartFile[] mediaFiles,
            Authentication authentication) {
        PostResponse updatedPost = postService.updatePost(id, title, content, authentication);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deletePost(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String message = postService.deletePost(id, userDetails.getUsername());
        MessageResponse messageResponse = new MessageResponse("SUCCESS", message);
        return ResponseEntity.ok(messageResponse);
    }

}
