package com.zerooneblog.api.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.domain.UserReport;
import com.zerooneblog.api.infrastructure.persistence.UserReportRepository;
import com.zerooneblog.api.interfaces.dto.requestDto.UserReportRequest;
// import com.zerooneblog.api.interfaces.exception.DuplicateResourceException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedActionException;

// Service for managing user reports and flagging inappropriate user behavior
@Service
public class UserReportService {
    private final UserReportRepository userReportRepository;
    private final UserService userService;

    public UserReportService(UserReportRepository userReportRepository, UserService userService) {
        this.userReportRepository = userReportRepository;
        this.userService = userService;
    }

    // Report a user for inappropriate behavior or harassment
    @Transactional
    public void reportUser(String userToReport, UserReportRequest userReportRequest, String username ) {
        User toBeReportedUser = userService.findByUsername(userToReport);
        User currentUser = userService.findByUsername(username);

        // Prevent users from reporting themselves
        if (toBeReportedUser.getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You cannot report yourself.");
        }

        // Create and save new user report with reason and details
        UserReport userReport = new UserReport();
        userReport.setReported(toBeReportedUser);
        userReport.setReporter(currentUser);
        userReport.setDetails(userReportRequest.getDetails());
        userReport.setReason(userReportRequest.getReason());
        userReportRepository.save(userReport);
        
        // Increment report counts for both users
        toBeReportedUser.setReportedCount(toBeReportedUser.getReportedCount() + 1L);
        currentUser.setReportingCount(toBeReportedUser.getReportingCount() + 1L);
    }

}