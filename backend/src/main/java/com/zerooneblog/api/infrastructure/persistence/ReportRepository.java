package com.zerooneblog.api.infrastructure.persistence;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zerooneblog.api.domain.PostReport;

@Repository
public interface ReportRepository extends JpaRepository<PostReport, Long> {
    boolean existsByPostIdAndReporterId(Long postId, Long reportedId);

    void deleteAllByPostId(Long postId);

    @Query("SELECT r FROM PostReport r LEFT JOIN FETCH r.reporter LEFT JOIN FETCH r.post")
    Page<PostReport> findAllWithDetails(Pageable pageable);

    @Modifying
    @Query("DELETE FROM PostReport r WHERE r.reporter.id = :reporterId")
    void deleteAllByReporterId(@Param("reporterId") Long reporterId);

}
