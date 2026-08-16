package com.medcore.hms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecurityProperties securityProperties;
    private final Clock clock;
    private final SecretKey signingKey;

    public JwtService(SecurityProperties securityProperties, Clock clock) {
        this.securityProperties = securityProperties;
        this.clock = clock;
        byte[] secretBytes = securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateAccessToken(Long userId, String role) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(securityProperties.getJwt().getAccessExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(securityProperties.getJwt().getIssuer())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("role", role)
                .signWith(signingKey)
                .compact();
    }

    public Optional<String> extractSubject(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!securityProperties.getJwt().getIssuer().equals(claims.getIssuer())) {
                return Optional.empty();
            }
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public Optional<String> extractRole(String token) {
        try {
            Claims claims = parseClaims(token);
            if (!securityProperties.getJwt().getIssuer().equals(claims.getIssuer())) {
                return Optional.empty();
            }
            return Optional.ofNullable(claims.get("role", String.class));
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public boolean isValid(String token) {
        return extractSubject(token).filter(subject -> !subject.isBlank()).isPresent();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(securityProperties.getJwt().getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}