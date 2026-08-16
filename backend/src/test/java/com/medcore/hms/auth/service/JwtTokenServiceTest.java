package com.medcore.hms.auth.service;

import com.medcore.hms.security.JwtTokenService;
import com.medcore.hms.security.SecurityProperties;
import com.medcore.hms.user.domain.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    private JwtTokenService service;
    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = propertiesWith(
                "01234567890123456789012345678901", Duration.ofMinutes(15));
        service = new JwtTokenService(properties, Clock.fixed(NOW, ZoneOffset.UTC));

        userAccount = mock(UserAccount.class);
        when(userAccount.getUsername()).thenReturn("alice");
        when(userAccount.getRoleNames()).thenReturn(Set.of("ADMIN"));
    }

    @Test
    void issuesTokenWithExpectedSubjectAndExpiry() {
        String token = service.issueAccessToken(userAccount);

        assertThat(service.extractUsername(token)).isEqualTo("alice");
        assertThat(service.getAccessTokenExpiresAt(token)).isEqualTo(NOW.plus(Duration.ofMinutes(15)));
    }

    @Test
    void validatesTokenForMatchingUser() {
        String token = service.issueAccessToken(userAccount);
        UserDetails details = User.withUsername("alice")
                .password("ignored")
                .authorities("ROLE_ADMIN")
                .build();

        assertThat(service.isTokenValid(token, details)).isTrue();
    }

    @Test
    void rejectsTokenForDifferentUser() {
        String token = service.issueAccessToken(userAccount);
        UserDetails details = User.withUsername("bob")
                .password("ignored")
                .authorities("ROLE_ADMIN")
                .build();

        assertThat(service.isTokenValid(token, details)).isFalse();
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        String token = service.issueAccessToken(userAccount);
        JwtTokenService otherService = new JwtTokenService(
                propertiesWith("abcdefghijklmnopqrstuvwxyz123456", Duration.ofMinutes(15)),
                Clock.fixed(NOW, ZoneOffset.UTC));
        UserDetails details = User.withUsername("alice").password("ignored").authorities("ROLE_ADMIN").build();

        assertThat(otherService.isTokenValid(token, details)).isFalse();
    }

    private SecurityProperties propertiesWith(String secret, Duration accessExpiration) {
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setSecret(secret);
        properties.getJwt().setAccessExpiration(accessExpiration);
        return properties;
    }
}