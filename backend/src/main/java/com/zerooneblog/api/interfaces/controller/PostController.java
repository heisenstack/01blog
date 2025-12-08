package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.interfaces.dto.PostDTO;
import com.zerooneblog.api.interfaces.dto.PostResponse;
import com.zerooneblog.api.interfaces.dto.PostAuthorResponse;
import com.zerooneblog.api.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private PostResponse mapToPostResponse(Post post) {
        PostAuthorResponse authorResponse = new PostAuthorResponse(
                post.getAuthor().getId(),
                post.getAuthor().getUsername());

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                authorResponse);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // System.out.println("Request: " + request + " UserDetails: " + userDetails);
        String username = userDetails.getUsername();
        Post createdPost = postService.createPost(request, username);
        PostResponse postResponse = mapToPostResponse(createdPost);
        return new ResponseEntity<>(postResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();

        List<PostResponse> responseList = posts.stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable(name = "id") Long id) {
        Post post = postService.getPostById(id);
        PostResponse postResponse = mapToPostResponse(post);
        return ResponseEntity.ok(postResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> updatePost(@PathVariable(name = "id") Long id,
            @RequestBody PostDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Post updatedPost = postService.updatePost(id, request, username);
        PostResponse postResponse = mapToPostResponse(updatedPost);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable(name= "id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        String message = postService.deletePost(id, username);
        return ResponseEntity.ok(message);
    }
    
}
