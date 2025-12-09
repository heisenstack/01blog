package com.zerooneblog.api.service;

import com.zerooneblog.api.interfaces.dto.CommentDTO;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;

import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.infrastructure.persistence.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.zerooneblog.api.domain.*;

// import com.zerooneblog.api.service.UserService;


@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userService = userService;
    }

    @Transactional
    public String createComment(Long postId, String content, String username) {
        Post post = postRepository.findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        User user = userService.findByUsername(username);
        Comment newComment = new Comment();
        newComment.setContent(content);
        newComment.setPost(post);
        newComment.setUser(user);
        commentRepository.save(newComment);
        return "Comment: " + newComment.getContent() + " on post: " + newComment.getPost().getId() + " has been created successfully!";
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        List<Comment> comments =  commentRepository.findByPostId(postId);
    return comments.stream()
        .map(this::mapToDto)
        .collect(Collectors.toList());    
    }

    private CommentDTO mapToDto(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setPostId(comment.getPost().getId());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUsername(comment.getPost().getAuthor().getUsername());
        return dto;
    } 
}
