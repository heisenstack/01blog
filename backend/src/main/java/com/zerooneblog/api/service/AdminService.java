package com.zerooneblog.api.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.infrastructure.persistence.*;
import com.zerooneblog.api.interfaces.dto.DashboardStatsDto;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;
    private final UserReportRepository userReportRepository;

    public AdminService(UserRepository userRepository, PostRepository postRepository, ReportRepository reportRepository, UserReportRepository userReportRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.reportRepository = reportRepository;
        this.userReportRepository = userReportRepository;
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
}
