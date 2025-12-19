package com.zerooneblog.api.interfaces.dto;

import com.zerooneblog.api.domain.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private String message;
    private boolean isRead;
    private Notification.NotificationType type;
    private String senderUsername;
    private Long postId;  
    private LocalDateTime createdAt;
}

