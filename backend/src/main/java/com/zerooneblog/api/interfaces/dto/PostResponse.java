package com.zerooneblog.api.interfaces.dto;

import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private Instant createdAt;
    private String authorUsername;
    private Long authorId;
    private long likeCount;
    private boolean isLikedByCurrentUser;
}
