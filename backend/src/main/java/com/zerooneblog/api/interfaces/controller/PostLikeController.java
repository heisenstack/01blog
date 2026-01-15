package com.zerooneblog.api.interfaces.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.zerooneblog.api.interfaces.dto.PostLikeResponseDto;
import com.zerooneblog.api.service.PostLikeService;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/posts/{postId}")
public class PostLikeController {
    private PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }
    
    @PostMapping("/like")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<PostLikeResponseDto> likePost(@PathVariable Long postId,
            Authentication authentication) {
        PostLikeResponseDto response = postLikeService.likePost(postId, authentication);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unlike")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<PostLikeResponseDto> unlikePost(@PathVariable Long postId,
            Authentication authentication) {
        PostLikeResponseDto response = postLikeService.unlikePost(postId, authentication);
        return ResponseEntity.ok(response);
    }

}
