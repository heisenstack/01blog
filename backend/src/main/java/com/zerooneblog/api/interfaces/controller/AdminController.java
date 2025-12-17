package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.interfaces.dto.*;
import com.zerooneblog.api.service.AdminService;
import com.zerooneblog.api.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostsResponseDto> getAllPostsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        PostsResponseDto posts = postService.getAllPostsForAdmin(page, size, authentication);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminViewResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserAdminViewResponse users = adminService.getAllUsersPaginated(page, size);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        adminService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ReportResponse reports = adminService.getAllReportsPaginated(page, size);
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/posts/{postId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> hidePost(@PathVariable Long postId) {
        try {
            PostResponse result = adminService.hidePost(postId);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/posts/{postId}/unhide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PostResponse> unhidePost(@PathVariable Long postId) {

        PostResponse result = adminService.unhidePost(postId);
        return ResponseEntity.ok(result);

    }

    @GetMapping("/posts/hidden")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HiddenPostsResponse> getHiddenPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PostResponse> hiddenPostsPage = postService.getHiddenPosts(page, size);

        HiddenPostsResponse response = new HiddenPostsResponse(
                hiddenPostsPage.getContent(),
                hiddenPostsPage.getNumber(),
                hiddenPostsPage.getSize(),
                hiddenPostsPage.getTotalElements(),
                hiddenPostsPage.getTotalPages(),
                hiddenPostsPage.isLast());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> banUser(@PathVariable Long userId) {

        adminService.banUser(userId);
        return ResponseEntity.noContent().build();

    }

    @PutMapping("/users/{userId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unbanUser(@PathVariable Long userId) {

        adminService.unbanUser(userId);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/users/banned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminViewResponse> getBannedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserAdminViewResponse bannedUsers = adminService.getBannedUsers(page, size);
        return ResponseEntity.ok(bannedUsers);
    }

        @GetMapping("/reports/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserReportResponse> getReportedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserReportResponse reportedUsers = adminService.getAllUserReportsPaginated(page, size);
        return ResponseEntity.ok(reportedUsers);
    }
    @DeleteMapping("/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> dismissReport(@PathVariable Long reportId) {
        adminService.dismissReport(reportId);
        return ResponseEntity.noContent().build();
    }
        @DeleteMapping("/reports/users/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> dismissUserReport(@PathVariable Long reportId) {
        adminService.dismissUserReport(reportId);
        return ResponseEntity.noContent().build();
    }


}