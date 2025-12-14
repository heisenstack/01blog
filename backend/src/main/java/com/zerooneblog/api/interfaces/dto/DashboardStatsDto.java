package com.zerooneblog.api.interfaces.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardStatsDto {
    private long totalUsers;
    private long totalPosts;
    private long activeReports;
    private long newUsersLast30Days;
    private long hiddenPosts;
    private long bannedUsers;
}