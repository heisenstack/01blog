package com.zerooneblog.api.service;

import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.api.domain.Notification;
import com.zerooneblog.api.domain.Post;
import com.zerooneblog.api.domain.User;
import com.zerooneblog.api.infrastructure.persistence.NotificationRepository;
import com.zerooneblog.api.infrastructure.persistence.UserRepository;
import com.zerooneblog.api.interfaces.dto.NotificationDto;

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
    public void createNotification(User recipient, User sender, Notification.NotificationType type, String message, Post post) {
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
        
        Notification savedNotification = notificationRepository.save(notification);
        
        NotificationDto notificationDto = mapToDto(savedNotification);
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
                notification.getCreatedAt()
        );
    }

}
