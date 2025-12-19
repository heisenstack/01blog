package com.zerooneblog.api.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zerooneblog.api.domain.Notification;
import com.zerooneblog.api.domain.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    long countByRecipient(User recipient);

    long countByRecipientAndIsRead(User recipient, boolean isRead);

    Page<Notification> findByRecipientAndIsReadOrderByCreatedAtDesc(
            User recipient,
            boolean isRead,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId AND n.isRead = false")
    void markAllAsReadForUser(@Param("userId") Long userId);
}
