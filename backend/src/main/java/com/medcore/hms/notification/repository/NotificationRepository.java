package com.medcore.hms.notification.repository;

import com.medcore.hms.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserAccountIdOrderByCreatedAtDesc(
            Long userAccountId,
            Pageable pageable);

    Optional<Notification> findByIdAndUserAccountId(
            Long id,
            Long userAccountId);

    long countByUserAccountIdAndReadAtIsNull(Long userAccountId);
}
