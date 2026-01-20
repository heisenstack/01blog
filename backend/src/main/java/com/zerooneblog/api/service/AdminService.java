package com.zerooneblog.api.service;

import com.zerooneblog.api.domain.*;
import com.zerooneblog.api.infrastructure.persistence.*;
import com.zerooneblog.api.service.mapper.*;
import com.zerooneblog.api.interfaces.dto.*;
import org.springframework.data.domain.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;

// Service for admin operations: managing users, posts, and reports
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;
    private final UserReportRepository userReportRepository;
    private final UserAdminViewMapper userAdminViewMapper;
    private final ReportMapper reportMapper;
    private final PostMapper postMapper;

    public AdminService(UserRepository userRepository, PostRepository postRepository, ReportRepository reportRepository,
            UserReportRepository userReportRepository, UserAdminViewMapper userAdminViewMapper,
            ReportMapper reportMapper, PostMapper postMapper) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.reportRepository = reportRepository;
        this.userReportRepository = userReportRepository;
        this.userAdminViewMapper = userAdminViewMapper;
        this.reportMapper = reportMapper;
        this.postMapper = postMapper;
    }

    // Get dashboard statistics (total users, posts, reports, banned users, etc.)
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long hiddenPosts = postRepository.countByHidden(true);
        long activeReports = reportRepository.count() + userReportRepository.count();
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        long newUsers = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long bannedUsers = userRepository.countByEnabled(false);
        stats.setTotalUsers(totalUsers);
        stats.setTotalPosts(totalPosts);
        stats.setHiddenPosts(hiddenPosts);
        stats.setActiveReports(activeReports);
        stats.setNewUsersLast30Days(newUsers);
        stats.setBannedUsers(bannedUsers);
        return stats;
    }

    // Get all users with pagination, excluding admin users
    @Transactional(readOnly = true)
    public UserAdminViewResponse getAllUsersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserAdminViewDto> userDtos = userPage.getContent().stream()
                .filter(user -> user.getRoles().stream().noneMatch(role -> role == Role.ADMIN))
                .map(user -> userAdminViewMapper.toDto(user))
                .collect(Collectors.toList());

        return new UserAdminViewResponse(
                userDtos,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast());

    }

    // Get all post reports with pagination
    @Transactional(readOnly = true)
    public ReportResponse getAllReportsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostReport> reportPage = reportRepository.findAllWithDetails(pageable);

        List<ReportDto> reportDtos = reportPage.getContent().stream()
                .map(report -> reportMapper.toDto(report))
                .collect(Collectors.toList());

        return new ReportResponse(
                reportDtos,
                reportPage.getNumber(),
                reportPage.getSize(),
                reportPage.getTotalElements(),
                reportPage.getTotalPages(),
                reportPage.isLast());
    }

    // Delete a post and all its associated reports
    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        reportRepository.deleteAllByPostId(postId);
        postRepository.deleteById(postId);
    }

    // Hide a post from public view
    @Transactional
    public PostResponse hidePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (post.isHidden()) {
            throw new IllegalStateException("Post is already hidden.");
        }

        post.setHidden(true);
        Post hiddenPost = postRepository.save(post);
        reportRepository.deleteAllByPostId(postId);
        return postMapper.toDto(hiddenPost, null);
    }

    // Unhide a previously hidden post
    @Transactional
    public PostResponse unhidePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!post.isHidden()) {
            throw new IllegalStateException("Post is not hidden.");
        }

        post.setHidden(false);
        Post unhiddenPost = postRepository.save(post);
        return postMapper.toDto(unhiddenPost, null);
    }

    // Ban a user account (prevent login and access)
    @Transactional
    public void banUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRoles().contains(Role.ADMIN)) {
            throw new IllegalStateException("Admin users cannot be banned.");
        }

        if (!user.isEnabled()) {
            throw new IllegalStateException("User is already banned.");
        }
        user.setEnabled(false);
        userRepository.save(user);
    }

    // Unban a previously banned user
    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.isEnabled()) {
            throw new IllegalStateException("User is not banned.");
        }

        user.setEnabled(true);
        userRepository.save(user);
    }

    // Get all banned users with pagination
    @Transactional(readOnly = true)
    public UserAdminViewResponse getBannedUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> bannedUsersPage = userRepository.findByEnabled(false, pageable);

        List<UserAdminViewDto> userDtos = bannedUsersPage.getContent().stream()
                .filter(user -> !user.getRoles().contains(Role.ADMIN))
                .map(user -> userAdminViewMapper.toDto(user))
                .collect(Collectors.toList());

        return new UserAdminViewResponse(
                userDtos,
                bannedUsersPage.getNumber(),
                bannedUsersPage.getSize(),
                (long) userDtos.size(),
                bannedUsersPage.getTotalPages(),
                bannedUsersPage.isLast());
    }

    // Get all user reports with pagination
    @Transactional(readOnly = true)
    public UserReportResponse getAllUserReportsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserReport> userReportPage = userReportRepository.findAll(pageable);

        List<UserReportDto> userReportDtos = userReportPage.getContent().stream()
                .map(this::toUserReportDto)
                .collect(Collectors.toList());

        return new UserReportResponse(
                userReportDtos,
                userReportPage.getNumber(),
                userReportPage.getSize(),
                userReportPage.getTotalElements(),
                userReportPage.getTotalPages(),
                userReportPage.isLast());
    }

    // Convert UserReport entity to DTO
    private UserReportDto toUserReportDto(UserReport userReport) {
        UserReportDto dto = new UserReportDto();
        dto.setId(userReport.getId());
        dto.setReason(userReport.getReason());
        dto.setDetails(userReport.getDetails());
        dto.setCreatedAt(userReport.getCreatedAt());
        dto.setReporterUsername(userReport.getReporter().getUsername());
        dto.setReportedUserId(userReport.getReported().getId());
        dto.setReportedUsername(userReport.getReported().getUsername());
        dto.setEnabled(userReport.getReported().isEnabled());

        return dto;
    }

    // Dismiss a post report (delete it)
    @Transactional
    public void dismissReport(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new ResourceNotFoundException("Report", "id", reportId);
        }
        reportRepository.deleteById(reportId);
    }

    // Dismiss a user report (delete it)
    @Transactional
    public void dismissUserReport(Long userReportId) {
        if (!userReportRepository.existsById(userReportId)) {
            throw new ResourceNotFoundException("UserReport", "id", userReportId);
        }
        userReportRepository.deleteById(userReportId);
    }

    // Delete a user account and all related data
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRoles().contains(Role.ADMIN)) {
            throw new IllegalStateException("Admin users cannot be deleted.");
        }

        userRepository.deleteUserRelationships(userId);

        userRepository.deleteById(userId);
    }

}