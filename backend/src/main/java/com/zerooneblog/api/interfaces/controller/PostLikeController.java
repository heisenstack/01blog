package com.zerooneblog.api.interfaces.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.zerooneblog.api.interfaces.dto.PostLikeResponseDto;
import com.zerooneblog.api.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/posts/{postId}")
public class PostLikeController {
    private PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @PostMapping("/like")
    public ResponseEntity<PostLikeResponseDto> likePost(@PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PostLikeResponseDto response = postLikeService.likePost(postId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unlike")
    public ResponseEntity<PostLikeResponseDto> unlikePost(@PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PostLikeResponseDto response = postLikeService.unlikePost(postId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

}
