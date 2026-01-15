package com.zerooneblog.api.interfaces.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.zerooneblog.api.interfaces.dto.*;
import com.zerooneblog.api.service.CommentService;

@RestController
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<CommentDTO> createComment(@PathVariable Long postId, @RequestBody Map<String, String> payload,
            Authentication authentication) {
        CommentDTO newComment = commentService.createComment(postId, payload.get("content"), authentication);
        return ResponseEntity.ok(newComment);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDto> getCommentsByPostId(@PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId, page, size));
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long commentId,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        CommentDTO updatedComment = commentService.updateComment(commentId, payload.get("content"),
                authentication);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<MessageResponse> deleteComment(@PathVariable Long commentId,
            Authentication authentication) {
        String message = commentService.deleteComment(commentId, authentication);
        MessageResponse response = new MessageResponse("SUCCESS", message);
        return ResponseEntity.ok(response);
    }
}
