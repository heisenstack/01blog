package com.zerooneblog.api.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String name;
    private String username;
    private PostsResponseDto posts;  
    private Long followerCount;
    private Long followingCount;
    private boolean subscribed;
    private boolean enabled = true;
    private Long reportedCount;
    private Long reportingCount;
}