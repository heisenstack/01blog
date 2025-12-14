package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.service.AdminService;
import com.zerooneblog.api.service.PostService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final PostService postService;

    public AdminController(AdminService adminService, PostService postService) {
        this.adminService = adminService;
        this.postService = postService;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
}