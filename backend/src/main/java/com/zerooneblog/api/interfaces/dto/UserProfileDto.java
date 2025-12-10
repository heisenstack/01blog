package com.zerooneblog.api.interfaces.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String fullName;
    private String username;
    private List<PostResponse> posts;
    private Long followerCount;
    private Long followingCount;
    private boolean subscribed;
    private boolean enabled = true;
}
