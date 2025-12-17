package com.zerooneblog.api.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.domain.PostReport;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.*;
import com.zerooneblog.api.interfaces.dto.DashboardStatsDto;
import com.zerooneblog.api.interfaces.dto.ReportDto;
import com.zerooneblog.api.interfaces.dto.ReportResponse;
import com.zerooneblog.api.interfaces.dto.UserAdminViewDto;
import com.zerooneblog.api.interfaces.dto.UserAdminViewResponse;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;
import com.zerooneblog.api.service.mapper.ReportMapper;
import com.zerooneblog.api.service.mapper.UserAdminViewMapper;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;
    private final UserReportRepository userReportRepository;
    private final UserAdminViewMapper userAdminViewMapper;
    private final ReportMapper reportMapper;

    public AdminService(UserRepository userRepository, PostRepository postRepository, ReportRepository reportRepository,
            UserReportRepository userReportRepository, UserAdminViewMapper userAdminViewMapper,
            ReportMapper reportMapper) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.reportRepository = reportRepository;
        this.userReportRepository = userReportRepository;
        this.userAdminViewMapper = userAdminViewMapper;
        this.reportMapper = reportMapper;
    }

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

    @Transactional(readOnly = true)
    public UserAdminViewResponse getAllUsersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserAdminViewDto> userDtos = userPage.getContent().stream()
                .filter(user -> user.getRoles().stream().noneMatch(role -> role.equals("ROLE_ADMIN")))
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

    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        reportRepository.deleteAllByPostId(postId);
        postRepository.deleteById(postId);
    }

}
