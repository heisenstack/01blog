package com.zerooneblog.api.service;

import com.zerooneblog.api.interfaces.dto.NotificationCountDto;
import com.zerooneblog.api.interfaces.dto.NotificationDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

// Service for sending real-time notifications via WebSocket
@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send a new notification to a specific user
     * @param username The recipient's username
     * @param notification The notification to send
     */
    public void sendNotificationToUser(String username, NotificationDto notification) {
        messagingTemplate.convertAndSendToUser(
            username, 
            "/queue/notifications", 
            notification
        );
    }

    /**
     * Send updated notification counts to a specific user
     * @param username The recipient's username
     * @param counts The updated counts
     */
    public void sendNotificationCountsToUser(String username, NotificationCountDto counts) {
        messagingTemplate.convertAndSendToUser(
            username, 
            "/queue/notification-counts", 
            counts
        );
    }

    /**
     * Notify user that a notification was marked as read
     * @param username The user's username
     * @param notificationId The notification ID that was read
     */
    public void notifyNotificationRead(String username, Long notificationId) {
        messagingTemplate.convertAndSendToUser(
            username, 
            "/queue/notification-read", 
            notificationId
        );
    }

    /**
     * Notify user that notifications were deleted
     * @param username The user's username
     * @param notificationIds The IDs of deleted notifications
     */
    public void notifyNotificationsDeleted(String username, java.util.List<Long> notificationIds) {
        messagingTemplate.convertAndSendToUser(
            username, 
            "/queue/notifications-deleted", 
            notificationIds
        );
    }
}