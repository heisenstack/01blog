package com.zerooneblog.api.interfaces.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDto {

    private Long id;
    private String reason;
    private String details;
    private LocalDateTime createdAt;
    private String reporterUsername;
    private Long reportedPostId;
    private String reportedPostTitle;
    
}
