package com.zerooneblog.api.interfaces.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private Long postId;
    private String username;
    private LocalDateTime createdAt;
   }