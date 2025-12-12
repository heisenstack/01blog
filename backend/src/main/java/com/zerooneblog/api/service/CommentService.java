package com.zerooneblog.api.service;

import com.zerooneblog.api.interfaces.dto.CommentResponseDto;
import com.zerooneblog.api.interfaces.dto.CommentDTO;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedActionException;
import com.zerooneblog.api.service.mapper.CommentMapper;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.*;
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
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserService userService, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userService = userService;
        this.commentMapper = commentMapper;
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
    public CommentResponseDto getCommentsByPostId(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> commentPage =commentRepository.findByPostId(postId, pageable);
        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
        .map(commentDto -> commentMapper.toDto(commentDto))
        .collect(Collectors.toList());
        return new CommentResponseDto(
            commentDTOs,
            commentPage.getNumber(),
            commentPage.getSize(),
            commentPage.getTotalElements(),
            commentPage.getTotalPages(),
            commentPage.isLast()
        );
    }

    @Transactional
    public CommentDTO updateComment(Long commentId, String content, String username) {
        Comment comment = commentRepository.findById(commentId)
        .orElseThrow(()-> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new UnauthorizedActionException("You are not authorized to update this comment.");
        }
        comment.setContent(content);
        Comment updaComment = commentRepository.save(comment);
        return commentMapper.toDto(updaComment);
    }

    @Transactional
    public String deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
         .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
         if (!comment.getUser().getUsername().equals(username)) {
            throw new UnauthorizedActionException("You are not authorized to delete this comment.");
         }
         commentRepository.delete(comment);
         return "The comment has been deleted successfully!";
    }

}
