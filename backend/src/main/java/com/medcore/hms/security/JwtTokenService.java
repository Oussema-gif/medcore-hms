package com.medcore.hms.security;

import com.medcore.hms.user.domain.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private static final String TOKEN_TYPE_CLAIM = "typ";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final SecurityProperties securityProperties;
    private final Clock clock;
    private final SecretKey signingKey;

    public JwtTokenService(SecurityProperties securityProperties, Clock clock) {
        this.securityProperties = securityProperties;
        this.clock = clock;
        this.signingKey = Keys.hmacShaKeyFor(
                securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(UserAccount userAccount) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(securityProperties.getJwt().getAccessExpiration());

        return Jwts.builder()
                .subject(userAccount.getUsername())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .claim("roles", List.copyOf(userAccount.getRoleNames()))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    public Instant getAccessTokenExpiresAt(String token) {
        Date expiration = parseClaims(token).getPayload().getExpiration();
        return expiration.toInstant();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parseClaims(token).getPayload();
            return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                    && claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration().after(Date.from(clock.instant()));
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token);
    }
}
