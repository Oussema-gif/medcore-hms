package com.medcore.hms.auth.service;

import com.medcore.hms.auth.domain.RefreshToken;
import com.medcore.hms.auth.repository.RefreshTokenRepository;
import com.medcore.hms.exception.UnauthorizedException;
import com.medcore.hms.security.SecurityProperties;
import com.medcore.hms.user.domain.UserAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private static final String INVALID_REFRESH_TOKEN = "Refresh token is invalid or expired";

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityProperties securityProperties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            SecurityProperties securityProperties,
            Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityProperties = securityProperties;
        this.clock = clock;
    }

    @Transactional
    public IssuedRefreshToken issue(UserAccount userAccount, String sourceIp, String userAgent) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(securityProperties.getJwt().getRefreshExpiration());
        String rawToken = generateRawToken();
        RefreshToken token = RefreshToken.create(
                userAccount,
                hash(rawToken),
                expiresAt,
                sourceIp,
                truncateUserAgent(userAgent),
                issuedAt);
        refreshTokenRepository.save(token);
        return new IssuedRefreshToken(rawToken, expiresAt);
    }

    @Transactional(readOnly = true)
    public UserAccount findUserByToken(String rawToken) {
        return findActive(rawToken, clock.instant()).getUserAccount();
    }

    @Transactional
    public IssuedRefreshToken rotate(String rawToken, String sourceIp, String userAgent) {
        Instant now = clock.instant();
        RefreshToken current = findActive(rawToken, now);
        UserAccount userAccount = current.getUserAccount();

        if (!userAccount.canAuthenticate(now)) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }

        IssuedRefreshToken replacement = issue(userAccount, sourceIp, userAgent);
        RefreshToken replacementEntity = refreshTokenRepository.findByTokenHash(hash(replacement.rawToken()))
                .orElseThrow(() -> new IllegalStateException("Issued refresh token could not be loaded"));
        current.replaceWith(replacementEntity, now);
        refreshTokenRepository.save(current);
        return replacement;
    }

    @Transactional
    public void revoke(String rawToken, String reason) {
        refreshTokenRepository.findByTokenHash(hash(rawToken))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> {
                    token.revoke(clock.instant(), reason);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public int revokeAllForUser(Long userId, String reason) {
        return refreshTokenRepository.revokeAllActiveForUser(userId, clock.instant(), reason);
    }

    private RefreshToken findActive(String rawToken, Instant now) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
        return refreshTokenRepository.findByTokenHash(hash(rawToken))
                .filter(token -> token.isActive(now))
                .orElseThrow(() -> new UnauthorizedException(INVALID_REFRESH_TOKEN));
    }

    private String generateRawToken() {
        byte[] tokenBytes = new byte[64];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        return userAgent.length() <= 512 ? userAgent : userAgent.substring(0, 512);
    }

    public record IssuedRefreshToken(String rawToken, Instant expiresAt) {
    }
}