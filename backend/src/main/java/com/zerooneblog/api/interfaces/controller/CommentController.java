package com.zerooneblog.api.interfaces.controller;


import com.zerooneblog.api.interfaces.dto.CommentDTO;
import com.zerooneblog.api.service.CommentService;


import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;




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

}
