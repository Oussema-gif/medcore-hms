package com.medcore.hms.auth.service;

import com.medcore.hms.auth.dto.LoginRequest;
import com.medcore.hms.auth.dto.TokenResponse;
import com.medcore.hms.auth.dto.UserSummary;
import com.medcore.hms.exception.UnauthorizedException;
import com.medcore.hms.security.JwtTokenService;
import com.medcore.hms.user.domain.UserAccount;
import com.medcore.hms.user.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS =
            "Invalid username or password";

    private static final String UNAVAILABLE_ACCOUNT =
            "User account is not available for authentication";

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final Clock clock;

    public AuthenticationService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService,
            Clock clock) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.clock = clock;
    }

    @Transactional
    public AuthenticatedSession authenticate(
            LoginRequest request,
            String sourceIp,
            String userAgent) {

        UserAccount userAccount = userAccountRepository
                .findByUsernameOrEmailIgnoreCase(
                        request.usernameOrEmail(),
                        request.usernameOrEmail())
                .filter(account -> passwordEncoder.matches(
                        request.password(),
                        account.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));

        Instant now = clock.instant();

        if (!userAccount.canAuthenticate(now)) {
            throw new UnauthorizedException(UNAVAILABLE_ACCOUNT);
        }

        userAccount.recordSuccessfulLogin(now);
        userAccountRepository.save(userAccount);

        String accessToken = jwtTokenService.issueAccessToken(userAccount);

        RefreshTokenService.IssuedRefreshToken refreshToken =
                refreshTokenService.issue(userAccount, sourceIp, userAgent);

        TokenResponse tokens = TokenResponse.bearer(
                accessToken,
                refreshToken.rawToken(),
                secondsUntil(jwtTokenService.getAccessTokenExpiresAt(accessToken)));

        return new AuthenticatedSession(
                tokens,
                toUserSummary(userAccount));
    }

    @Transactional
    public TokenResponse refresh(
            String rawRefreshToken,
            String sourceIp,
            String userAgent) {

        UserAccount userAccount =
                refreshTokenService.findUserByToken(rawRefreshToken);

        RefreshTokenService.IssuedRefreshToken replacement =
                refreshTokenService.rotate(
                        rawRefreshToken,
                        sourceIp,
                        userAgent);

        String accessToken = jwtTokenService.issueAccessToken(userAccount);

        return TokenResponse.bearer(
                accessToken,
                replacement.rawToken(),
                secondsUntil(jwtTokenService.getAccessTokenExpiresAt(accessToken)));
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken, "LOGOUT");
    }

    private UserSummary toUserSummary(UserAccount userAccount) {
        return new UserSummary(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getEmail(),
                userAccount.getDisplayName(),
                userAccount.getRoleNames());
    }

    private long secondsUntil(Instant expiresAt) {
        return Math.max(
                0,
                expiresAt.getEpochSecond() - clock.instant().getEpochSecond());
    }

    public record AuthenticatedSession(
            TokenResponse tokens,
            UserSummary user) {
    }
}