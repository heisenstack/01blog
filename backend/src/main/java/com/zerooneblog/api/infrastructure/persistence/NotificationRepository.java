package com.zerooneblog.api.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zerooneblog.api.domain.Notification;
import com.zerooneblog.api.domain.User;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);

    Page<Notification> findByRecipientAndIsReadOrderByCreatedAtDesc(
            User recipient,
            boolean isRead,
            Pageable pageable);
}
