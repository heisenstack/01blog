package com.zerooneblog.api.service.mapper;

import org.springframework.stereotype.Component;

import com.zerooneblog.api.domain.PostReport;
import com.zerooneblog.api.interfaces.dto.ReportDto;


@Component
public class ReportMapper {
        public ReportDto toDto(PostReport report) {
        ReportDto dto = new ReportDto();
        dto.setId(report.getId());
        dto.setReason(report.getReason());
        dto.setDetails(report.getDetails());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setReporterUsername(report.getReporter().getUsername());
        if (report.getPost() != null) {
            dto.setReportedPostId(report.getPost().getId());
            dto.setReportedPostTitle(report.getPost().getTitle());
        }
        return dto;
    }
    
}
