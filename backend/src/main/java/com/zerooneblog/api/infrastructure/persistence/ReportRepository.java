package com.zerooneblog.api.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zerooneblog.api.domain.PostReport;

@Repository
public interface ReportRepository extends JpaRepository<PostReport, Long> {
    boolean existsByPostIdAndReporterId(Long postId, Long reportedId);

    void deleteAllByPostId(Long postId);

    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.reporter LEFT JOIN FETCH r.post")
    Page<PostReport> findAllWithDetails(Pageable pageable);

}
