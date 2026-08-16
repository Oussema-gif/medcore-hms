package com.medcore.hms.auth.domain;

import com.medcore.hms.user.domain.UserAccount;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RefreshTokenTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-15T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-02-15T10:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-20T10:00:00Z");

    @Test
    void newlyCreatedTokenIsActiveBeforeExpiry() {
        RefreshToken token = token(EXPIRES_AT);

        assertThat(token.isActive(NOW)).isTrue();
        assertThat(token.getRevokedAt()).isNull();
        assertThat(token.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void expiredTokenIsInactive() {
        RefreshToken token = token(NOW);

        assertThat(token.isActive(NOW)).isFalse();
    }

    @Test
    void revokedTokenIsInactiveAndStoresReason() {
        RefreshToken token = token(EXPIRES_AT);
        token.revoke(NOW, "LOGOUT");

        assertThat(token.isActive(NOW)).isFalse();
        assertThat(token.getRevokedAt()).isEqualTo(NOW);
        assertThat(token.getRevokeReason()).isEqualTo("LOGOUT");
    }

    @Test
    void replacementLinksTokensAndMarksOriginalAsRotated() {
        RefreshToken original = token(EXPIRES_AT);
        RefreshToken replacement = token(EXPIRES_AT.plusSeconds(3600));

        original.replaceWith(replacement, NOW);

        assertThat(original.getReplacedByToken()).isSameAs(replacement);
        assertThat(original.getRevokedAt()).isEqualTo(NOW);
        assertThat(original.getRevokeReason()).isEqualTo("ROTATED");
    }

    private RefreshToken token(Instant expiresAt) {
        return RefreshToken.create(
                mock(UserAccount.class),
                "a".repeat(64),
                expiresAt,
                "192.0.2.10",
                "JUnit",
                CREATED_AT);
    }
}