package com.zerooneblog.api.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zerooneblog.api.domain.PostReport;

@Repository
public interface ReportRepository extends JpaRepository<PostReport, Long>{
    boolean existsByPostIdAndReporterId(Long postId, Long reportedId);
    void deleteAllByPostId(Long postId);

}
