package com.zerooneblog.api.interfaces.dto.requestDto;

import lombok.Data;

@Data
public class PostRequest {
    private String title;
    private String content;
}