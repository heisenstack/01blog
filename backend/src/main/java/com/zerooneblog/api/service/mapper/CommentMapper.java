package com.zerooneblog.api.service.mapper;

import com.zerooneblog.api.domain.Comment;
import com.zerooneblog.api.interfaces.dto.CommentDTO;

public class CommentMapper {
    
        public CommentDTO toDto(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setPostId(comment.getPost().getId());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUsername(comment.getPost().getAuthor().getUsername());
        return dto;
    } 
}
