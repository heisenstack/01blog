package com.zerooneblog.api.interfaces.controller;

import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.interfaces.dto.NotificationCountDto;
import com.zerooneblog.api.interfaces.dto.NotificationDto;
import com.zerooneblog.api.interfaces.exception.UnauthorizedOperationException;
import com.zerooneblog.api.service.NotificationService;
import com.zerooneblog.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Page;

// Endpoints for managing user notifications
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

    // Get paginated notifications with filtering (all, read, unread)
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

    // Mark a single notification as read
    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<NotificationCountDto> markNotificationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markNotificationAsRead(id, userDetails.getUsername());
        NotificationCountDto counts = notificationService.getNotificationCounts(userDetails.getUsername());
        return ResponseEntity.ok(counts);
    }

    // Get notification counts (read and unread)
    @GetMapping("/counts")
    public ResponseEntity<NotificationCountDto> getNotificationCounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        NotificationCountDto counts = notificationService.getNotificationCounts(userDetails.getUsername());
        return ResponseEntity.ok(counts);
    }

    // Mark all notifications as read for current user
    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<NotificationCountDto> markAllNotificationsAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        notificationService.markAllAsRead(currentUser.getId());
        NotificationCountDto counts = notificationService.getNotificationCounts(userDetails.getUsername());
        return ResponseEntity.ok(counts);
    }

    // Delete multiple notifications
    @DeleteMapping
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<NotificationCountDto> deleteNotifications(
            @RequestBody List<Long> notificationIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            notificationService.deleteNotifications(notificationIds, userDetails.getUsername());
            NotificationCountDto counts = notificationService.getNotificationCounts(userDetails.getUsername());
            return ResponseEntity.ok(counts);
        } catch (UnauthorizedOperationException e) {
            return ResponseEntity.status(403).build();
        }
    }
    
    // Get all unread notifications for current user
    @GetMapping
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<NotificationDto> notifications = notificationService.getUnreadNotifications(userDetails.getUsername());
        return ResponseEntity.ok(notifications);
    }
}