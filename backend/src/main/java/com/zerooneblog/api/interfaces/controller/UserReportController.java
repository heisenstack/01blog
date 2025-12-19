package com.zerooneblog.api.interfaces.controller;

import org.springframework.web.bind.annotation.*;

import com.zerooneblog.api.interfaces.dto.MessageResponse;
import com.zerooneblog.api.interfaces.dto.requestDto.UserReportRequest;
import com.zerooneblog.api.service.UserReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/users")
public class UserReportController {
    private final UserReportService userReportService;

    public UserReportController(UserReportService userReportService) {
        this.userReportService = userReportService;
    }

    @PostMapping("/{username}/report")
    public ResponseEntity<MessageResponse> reportUser(@PathVariable String username,
            @RequestBody UserReportRequest userReportRequest, @AuthenticationPrincipal UserDetails userDetails) {
        userReportService.reportUser(username, userReportRequest, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("SUCCESS", "Report submitted successfully!"));
    }
}
