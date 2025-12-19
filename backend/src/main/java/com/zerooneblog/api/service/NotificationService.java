package com.zerooneblog.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.domain.Notification;
import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.NotificationRepository;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.dto.NotificationCountDto;
import com.zerooneblog.api.interfaces.dto.NotificationDto;
import com.zerooneblog.api.interfaces.exception.NotificationNotFoundException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedActionException;
import com.zerooneblog.api.interfaces.exception.UnauthorizedOperationException;

@Service
public class NotificationService {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;

    }

    @Transactional
    public void createNotification(User recipient, User sender, Notification.NotificationType type, String message,
            Post post) {
        if (recipient.getId().equals(sender.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setType(type);
        notification.setMessage(message);
        notification.setPost(post);
        notification.setRead(false);

        notificationRepository.save(notification);

        // NotificationDto notificationDto = mapToDto(savedNotification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByReadStatus(String username, boolean isRead, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(
                user,
                isRead,
                pageable);

        return notifications.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getAllNotificationsPaginated(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable);

        return notifications.map(this::mapToDto);
    }

    private NotificationDto mapToDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getType(),
                notification.getSender().getUsername(),
                notification.getPost() != null ? notification.getPost().getId() : null,
                notification.getCreatedAt());
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(
                        () -> new NotificationNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().getUsername().equals(username)) {
            throw new UnauthorizedActionException("You are not authorized to access this notification.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    @Transactional(readOnly = true)
    public NotificationCountDto getNotificationCounts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        long totalCount = notificationRepository.countByRecipient(user);
        long unreadCount = notificationRepository.countByRecipientAndIsRead(user, false);
        long readCount = notificationRepository.countByRecipientAndIsRead(user, true);

        return new NotificationCountDto(totalCount, unreadCount, readCount);
    }

    @Transactional
    public void deleteNotification(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(
                        () -> new NotificationNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getRecipient().getUsername().equals(username)) {
            throw new UnauthorizedOperationException("You are not authorized to delete this notification.");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteNotifications(List<Long> notificationIds, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<Notification> notifications = notificationRepository.findAllById(notificationIds);

        notifications.forEach(notification -> {
            if (!notification.getRecipient().getId().equals(user.getId())) {
                throw new UnauthorizedOperationException("You are not authorized to delete these notifications.");
            }
        });

        notificationRepository.deleteAll(notifications);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notifications = notificationRepository
                .findByRecipientAndIsReadOrderByCreatedAtDesc(user, false, pageable);

        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public void createNotificationsForFollowers(List<User> followers, User sender, Notification.NotificationType type, String message, Post post) {
        List<Notification> notifications = followers.stream()
                .filter(follower -> !follower.getId().equals(sender.getId())) 
                .map(follower -> {
                    Notification notification = new Notification();
                    notification.setRecipient(follower);
                    notification.setSender(sender);
                    notification.setType(type);
                    notification.setMessage(message);
                    notification.setPost(post);
                    notification.setRead(false);
                    return notification;
                })
                .collect(Collectors.toList());
        
        notificationRepository.saveAll(notifications);
    }
}
