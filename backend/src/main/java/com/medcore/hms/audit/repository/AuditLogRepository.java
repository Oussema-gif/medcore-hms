package com.medcore.hms.audit.repository;

import com.medcore.hms.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByActorUserIdOrderByOccurredAtDesc(
            Long actorUserId,
            Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
            String entityType,
            Long entityId,
            Pageable pageable);
}
