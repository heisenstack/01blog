package com.zerooneblog.api.interfaces.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class PostDTO {
    private String title;
    private String content;
    private MultipartFile[] mediaFiles;
}