package com.zerooneblog.api.service;

import com.zerooneblog.api.interfaces.dto.CommentResponseDto;
import com.zerooneblog.api.interfaces.dto.CommentDTO;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedActionException;
import com.zerooneblog.api.service.mapper.CommentMapper;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;

import com.zerooneblog.api.infrastructure.persistence.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.zerooneblog.api.domain.*;

// Service for managing post comments
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;
    private final PostService postService;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserService userService,
            CommentMapper commentMapper, NotificationService notificationService, PostService postService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userService = userService;
        this.commentMapper = commentMapper;
        this.notificationService = notificationService;
        this.postService = postService;
    }

    // Create a new comment on a post
    @Transactional
    public CommentDTO createComment(Long postId, String content, Authentication authentication) {
        postService.validatePostAccess(postId, authentication);

        // Validate comment content is not empty
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty or contain only whitespace");
        }
        String username = authentication.getName();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        User user = userService.findByUsername(username);
        
        // Create and save new comment
        Comment newComment = new Comment();
        newComment.setContent(content.trim());
        newComment.setPost(post);
        newComment.setUser(user);
        Comment savedComment = commentRepository.save(newComment);
        
        // Notify post author about new comment
        User postAuthor = post.getAuthor();
        String message = user.getUsername() + " commented on your post: \"" + post.getTitle() + "\"";
        notificationService.createNotification(
                postAuthor,
                user,
                Notification.NotificationType.NEW_COMMENT,
                message,
                post);
        return commentMapper.toDto(savedComment);
    }

    // Get all comments for a post with pagination
    @Transactional(readOnly = true)
    public CommentResponseDto getCommentsByPostId(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

        // Convert comments to DTOs
        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
                .map(commentDto -> commentMapper.toDto(commentDto))
                .collect(Collectors.toList());
        
        return new CommentResponseDto(
                commentDTOs,
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isLast());
    }

    // Update a comment (only by comment author)
    @Transactional
    public CommentDTO updateComment(Long commentId, String content, Authentication authentication) {
        postService.validatePostAccess(commentId, authentication);
        
        // Validate updated content is not empty
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty or contain only whitespace");
        }
        String username = authentication.getName();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        Long postId = comment.getPost().getId();
        postService.validatePostAccess(postId, authentication);
        
        // Check if user is comment author
        if (!comment.getUser().getUsername().equals(username)) {
            throw new UnauthorizedActionException("You are not authorized to update this comment.");
        }
        
        // Update comment content
        comment.setContent(content);
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDto(updatedComment);
    }

    // Delete a comment (only by comment author)
    @Transactional
    public String deleteComment(Long commentId, Authentication authentication) {
        String username = authentication.getName();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        Long postId = comment.getPost().getId();
        postService.validatePostAccess(postId, authentication);
        
        // Check if user is comment author
        if (!comment.getUser().getUsername().equals(username)) {
            throw new UnauthorizedActionException("You are not authorized to delete this comment.");
        }
        
        // Delete comment
        commentRepository.delete(comment);
        return "The comment has been deleted successfully!";
    }

}