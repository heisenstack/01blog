package com.zerooneblog.api.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.domain.*;
import com.zerooneblog.api.infrastructure.persistence.PostRepository;
import com.zerooneblog.api.infrastructure.persistence.ReportRepository;
import com.zerooneblog.api.interfaces.dto.requestDto.ReportRequestDto;
import com.zerooneblog.api.interfaces.exception.ResourceNotFoundException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedActionException;

// Service for managing post reports and flagging inappropriate content
@Service
public class PostReportService {
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostService postService;

    public PostReportService(ReportRepository reportRepository, PostRepository postRepository, UserService userService, PostService postService) {
        this.reportRepository = reportRepository;
        this.postRepository = postRepository;
        this.userService = userService;
        this.postService = postService;
    }

    // Report a post for inappropriate content or violation
    @Transactional
    public String reportPost(Long postId, ReportRequestDto reportRequestDto, Authentication authentication) {
        postService.validatePostAccess(postId, authentication);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post", "id", postId));

        User currentUser = userService.getCurrentUserFromAuthentication(authentication);
        
        // Prevent users from reporting their own posts
        if (post.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not authorized to report your own post.");
        }
        
        // Create new post report with reason and details
        PostReport postReport = new PostReport();
        postReport.setPost(post);
        postReport.setReason(reportRequestDto.getReason());
        postReport.setDetails(reportRequestDto.getDetials());
        postReport.setReported(post.getAuthor());
        postReport.setReporter(currentUser);
        
        // Increment report count on post
        if (post.getReportedCount() == null) {
            post.setReportedCount(1L);
        } else {
            post.setReportedCount(post.getReportedCount() + 1L);
        }
        
        // Save report to database
        reportRepository.save(postReport);
        
        return "The post with id: " + postId + " has been reported successfully! Reports count: "
                + post.getReportedCount();
    }

}