package com.zerooneblog.api.interfaces.controller;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.api.interfaces.dto.CommentDTO;
import com.zerooneblog.api.service.CommentService;




@RestController
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<String> createComment(@PathVariable Long postId,@RequestBody String payload, @AuthenticationPrincipal UserDetails userDetails) {
        String newComment = commentService.createComment(postId, payload, userDetails.getUsername());
        return new ResponseEntity<>(newComment, HttpStatus.CREATED);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }
    
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long commentId,@RequestBody String payload,  @AuthenticationPrincipal UserDetails userDetails) {
        CommentDTO updatedComment = commentService.updateComment(commentId, payload, userDetails.getUsername());
        return ResponseEntity.ok(updatedComment);
    }
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetails userDetails) {
        String deletedComment = commentService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok(deletedComment);
    } 
}
