package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.interfaces.dto.NotificationDto;
import com.zerooneblog.api.service.NotificationService;
import com.zerooneblog.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(
            NotificationService notificationService, 
            UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<NotificationDto>> getNotificationsPaginated(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Page<NotificationDto> notifications;
        
        switch (filter.toLowerCase()) {
            case "unread":
                notifications = notificationService.getNotificationsByReadStatus(
                    userDetails.getUsername(), false, page, size);
                break;
            case "read":
                notifications = notificationService.getNotificationsByReadStatus(
                    userDetails.getUsername(), true, page, size);
                break;
            default:
                notifications = notificationService.getAllNotificationsPaginated(
                    userDetails.getUsername(), page, size);
        }
        
        return ResponseEntity.ok(notifications);
    }

    
}