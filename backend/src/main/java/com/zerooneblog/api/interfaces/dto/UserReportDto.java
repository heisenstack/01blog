package com.zerooneblog.api.interfaces.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.zerooneblog.api.domain.ReportReason;

@Getter
@Setter
@NoArgsConstructor
public class UserReportDto {
    private Long id;
    private ReportReason reason;
    private String details;
    private LocalDateTime createdAt;
    private String reporterUsername;
    private String reportedUsername;
    private Long reportedUserId; 
    private boolean enabled;
}