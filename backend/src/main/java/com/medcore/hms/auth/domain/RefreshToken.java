package com.medcore.hms.auth.domain;

import com.medcore.hms.user.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "revoke_reason", length = 128)
    private String revokeReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_by_token_id")
    private RefreshToken replacedByToken;

    @Column(name = "created_ip", length = 45)
    private String createdIp;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void revoke(Instant revokedAt, String reason) {
        this.revokedAt = revokedAt;
        this.revokeReason = reason;
    }

    public void replaceWith(RefreshToken replacement, Instant revokedAt) {
        this.replacedByToken = replacement;
        revoke(revokedAt, "ROTATED");
    }

    public static RefreshToken create(
            UserAccount userAccount,
            String tokenHash,
            Instant expiresAt,
            String createdIp,
            String userAgent,
            Instant createdAt) {
        RefreshToken token = new RefreshToken();
        token.userAccount = userAccount;
        token.tokenHash = tokenHash;
        token.expiresAt = expiresAt;
        token.createdIp = createdIp;
        token.userAgent = userAgent;
        token.createdAt = createdAt;
        return token;
    }
}