package com.zerooneblog.api.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zerooneblog.api.domain.UserReport;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    
}
