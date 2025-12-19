package com.zerooneblog.api.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zerooneblog.api.domain.UserReport;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    boolean existsByReportedIdAndReporterId(Long reportedUserId, Long reporterUserId);

    @Modifying
    @Query("DELETE FROM UserReport r WHERE r.reporter.id = :reporterId")
    void deleteAllByReporterId(@Param("reporterId") Long reporterId);

    @Modifying
    @Query("DELETE FROM UserReport r WHERE r.reported.id = :reportedId")
    void deleteAllByReportedId(@Param("reportedId") Long reportedId);
}
