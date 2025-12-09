package com.zerooneblog.api.service;

import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;
import com.zerooneblog.api.infrastructure.persistence.*;
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
}
