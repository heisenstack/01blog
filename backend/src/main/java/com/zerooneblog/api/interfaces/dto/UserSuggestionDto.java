package com.zerooneblog.api.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSuggestionDto {
    private Long id;
    private String username;
    private String name;
    private long followerCount;
    private boolean subscribed;

}