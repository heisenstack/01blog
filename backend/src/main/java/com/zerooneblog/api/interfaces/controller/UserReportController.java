package com.zerooneblog.api.interfaces.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.api.interfaces.dto.requestDto.UserReportRequest;
import com.zerooneblog.api.service.UserReportService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/users")
public class UserReportController {
    private final UserReportService userReportService;

    public UserReportController(UserReportService userReportService) {
        this.userReportService = userReportService;
    }


    @PostMapping("/{userId}/report")
    public ResponseEntity<String> reportUser(@PathVariable Long userId,@RequestBody UserReportRequest userReportRequest,@AuthenticationPrincipal UserDetails userDetails) {
        userReportService.reportUser(userId, userReportRequest, userDetails.getUsername());
        return ResponseEntity.ok("Report submitted successfully!");
    }
    
}
