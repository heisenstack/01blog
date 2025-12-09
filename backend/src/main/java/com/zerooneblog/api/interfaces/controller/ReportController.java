package com.zerooneblog.api.interfaces.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.zerooneblog.api.interfaces.dto.requestDto.ReportRequestDto;
import com.zerooneblog.api.service.ReportService;

@RestController
@RequestMapping("/api/posts")
public class ReportController {
    private final ReportService reportService;
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/{postId}/report")
    public ResponseEntity<String> reportPost(@PathVariable Long postId, @RequestBody ReportRequestDto reportRequest, Authentication authentication) {
          return ResponseEntity.ok(reportService.reportPost(postId, reportRequest, authentication));
    }
}
