package com.medcore.hms.auth.service;

import com.medcore.hms.auth.domain.RefreshToken;
import com.medcore.hms.auth.repository.RefreshTokenRepository;
import com.medcore.hms.security.SecurityProperties;
import com.medcore.hms.user.domain.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    private RefreshTokenRepository repository;
    private RefreshTokenService service;
    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        repository = mock(RefreshTokenRepository.class);
        userAccount = mock(UserAccount.class);

        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setSecret("01234567890123456789012345678901");
        properties.getJwt().setRefreshExpiration(java.time.Duration.ofDays(30));

        service = new RefreshTokenService(
                repository,
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void issueGeneratesOpaqueTokenAndStoresOnlyItsHash() {
        when(repository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.IssuedRefreshToken issued = service.issue(
                userAccount,
                "192.0.2.10",
                "JUnit");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        RefreshToken persisted = captor.getValue();

        assertThat(issued.rawToken()).isNotBlank();
        assertThat(persisted.getTokenHash()).hasSize(64);
        assertThat(persisted.getTokenHash()).doesNotContain(issued.rawToken());
        assertThat(persisted.getExpiresAt()).isEqualTo(NOW.plusSeconds(30 * 24 * 60 * 60));
        assertThat(persisted.getCreatedIp()).isEqualTo("192.0.2.10");
    }

    @Test
    void revokeMarksPersistedActiveToken() {
        RefreshToken token = RefreshToken.create(
                userAccount,
                "a".repeat(64),
                NOW.plusSeconds(3600),
                "192.0.2.10",
                "JUnit",
                NOW);
        when(repository.findByTokenHash(any())).thenReturn(Optional.of(token));

        service.revoke("raw-refresh-token", "LOGOUT");

        assertThat(token.getRevokedAt()).isEqualTo(NOW);
        assertThat(token.getRevokeReason()).isEqualTo("LOGOUT");
        verify(repository).save(token);
    }
}