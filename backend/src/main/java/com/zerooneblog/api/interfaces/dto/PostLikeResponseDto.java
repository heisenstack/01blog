package com.zerooneblog.api.interfaces.dto;

import lombok.Data;

@Data
public class PostLikeResponseDto {
    private Long likeCount;
    private boolean isLikedByCurrentUser;

    public PostLikeResponseDto(Long likeCount, boolean isLikedByCurrentUser) {
        this.likeCount = likeCount;
        this.isLikedByCurrentUser = isLikedByCurrentUser;
    }
}
