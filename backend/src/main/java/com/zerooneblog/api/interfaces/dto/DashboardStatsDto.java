package com.zerooneblog.api.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DashboardStatsDto {
    private Long totalUsers;
    private Long totalPosts;
    private Long hiddenPosts;
    private Long activeReports;
    private Long newUsersLast30Days;
    private Long bannedUsers;
    
    private TopUserDto mostReportedUser;
    private TopUserDto mostReporterUser;
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class TopUserDto {
        private Long userId;
        private String username;
        private Long count;
    }
}