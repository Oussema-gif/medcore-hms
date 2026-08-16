package com.medcore.hms.auth.service;

import com.medcore.hms.auth.dto.LoginRequest;
import com.medcore.hms.exception.UnauthorizedException;
import com.medcore.hms.security.JwtTokenService;
import com.medcore.hms.user.domain.UserAccount;
import com.medcore.hms.user.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    private UserAccountRepository userAccountRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenService jwtTokenService;
    private RefreshTokenService refreshTokenService;
    private AuthenticationService service;
    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        userAccountRepository = mock(UserAccountRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtTokenService = mock(JwtTokenService.class);
        refreshTokenService = mock(RefreshTokenService.class);
        service = new AuthenticationService(
                userAccountRepository,
                passwordEncoder,
                jwtTokenService,
                refreshTokenService,
                Clock.fixed(NOW, ZoneOffset.UTC));

        userAccount = mock(UserAccount.class);
        when(userAccount.getId()).thenReturn(1L);
        when(userAccount.getUsername()).thenReturn("alice");
        when(userAccount.getEmail()).thenReturn("alice@example.com");
        when(userAccount.getDisplayName()).thenReturn("Alice");
        when(userAccount.getRoleNames()).thenReturn(Set.of("ADMIN"));
        when(userAccount.getPasswordHash()).thenReturn("encoded-password");
        when(userAccount.canAuthenticate(NOW)).thenReturn(true);
        when(userAccountRepository.findByUsernameOrEmailIgnoreCase("alice", "alice"))
                .thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtTokenService.issueAccessToken(userAccount)).thenReturn("access-token");
        when(jwtTokenService.getAccessTokenExpiresAt("access-token"))
                .thenReturn(NOW.plusSeconds(900));
        when(refreshTokenService.issue(userAccount, "192.0.2.10", "JUnit"))
                .thenReturn(new RefreshTokenService.IssuedRefreshToken(
                        "refresh-token", NOW.plusSeconds(2_592_000)));
    }

    @Test
    void authenticatesValidCredentialsAndIssuesBothTokens() {
        AuthenticationService.AuthenticatedSession session = service.authenticate(
                new LoginRequest("alice", "plain-password"),
                "192.0.2.10",
                "JUnit");

        assertThat(session.tokens().accessToken()).isEqualTo("access-token");
        assertThat(session.tokens().refreshToken()).isEqualTo("refresh-token");
        assertThat(session.user().username()).isEqualTo("alice");
        assertThat(session.tokens().expiresIn()).isEqualTo(900);
        verify(refreshTokenService).issue(userAccount, "192.0.2.10", "JUnit");
    }

    @Test
    void rejectsInvalidPassword() {
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(
                new LoginRequest("alice", "wrong-password"),
                "192.0.2.10",
                "JUnit"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void rejectsUnavailableAccount() {
        when(userAccount.canAuthenticate(NOW)).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(
                new LoginRequest("alice", "plain-password"),
                "192.0.2.10",
                "JUnit"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User account is not available for authentication");
    }
}