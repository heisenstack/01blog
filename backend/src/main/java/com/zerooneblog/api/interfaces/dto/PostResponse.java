package com.zerooneblog.api.interfaces.dto;

import java.time.Instant;
import java.util.List;


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
    private long reportedCount;
    private boolean isLikedByCurrentUser;
    private List<PostMediaDto> mediaFiles;
    private boolean hidden;

}
