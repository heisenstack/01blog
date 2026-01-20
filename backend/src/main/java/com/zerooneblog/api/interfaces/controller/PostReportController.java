package com.zerooneblog.api.interfaces.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.zerooneblog.api.interfaces.dto.requestDto.ReportRequestDto;
import com.zerooneblog.api.service.PostReportService;

// Endpoints for reporting posts
@RestController
@RequestMapping("/api/posts")
public class PostReportController {
    private final PostReportService reportService;

    public PostReportController(PostReportService reportService) {
        this.reportService = reportService;
    }

    // Report a post for inappropriate content
    @PostMapping("/{postId}/report")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<String> reportPost(@PathVariable Long postId, @RequestBody ReportRequestDto reportRequest,
            Authentication authentication) {
        return ResponseEntity.ok(reportService.reportPost(postId, reportRequest, authentication));
    }
}