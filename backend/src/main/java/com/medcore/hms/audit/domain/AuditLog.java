package com.medcore.hms.audit.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(nullable = false, length = 128)
    private String action;

    @Column(name = "entity_type", length = 128)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AuditOutcome outcome;

    @Column(name = "source_ip", length = 45)
    private String sourceIp;

    @Column(name = "request_id", length = 128)
    private String requestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", columnDefinition = "json")
    private JsonNode metadataJson;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @PrePersist
    protected void onCreate() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    public static AuditLog create(
            Long actorUserId,
            String action,
            String entityType,
            Long entityId,
            AuditOutcome outcome,
            String sourceIp,
            String requestId,
            JsonNode metadataJson) {

        AuditLog auditLog = new AuditLog();
        auditLog.actorUserId = actorUserId;
        auditLog.action = action;
        auditLog.entityType = entityType;
        auditLog.entityId = entityId;
        auditLog.outcome = outcome;
        auditLog.sourceIp = sourceIp;
        auditLog.requestId = requestId;
        auditLog.metadataJson = metadataJson;
        return auditLog;
    }
}
