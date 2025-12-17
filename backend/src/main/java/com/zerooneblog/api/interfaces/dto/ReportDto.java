package com.zerooneblog.api.interfaces.dto;

import java.time.LocalDateTime;

import com.zerooneblog.api.domain.ReportReason;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDto {

    private Long id;
    private ReportReason reason;
    private String details;
    private LocalDateTime createdAt;
    private String reporterUsername;
    private Long reportedPostId;
    private String reportedPostTitle;
    
}
