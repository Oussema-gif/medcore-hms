package com.medcore.hms.auth.repository;

import com.medcore.hms.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByTokenHashAndUserAccountId(String tokenHash, Long userAccountId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshToken token
               set token.revokedAt = :revokedAt,
                   token.revokeReason = :reason
             where token.userAccount.id = :userId
               and token.revokedAt is null
               and token.expiresAt > :revokedAt
            """)
    int revokeAllActiveForUser(
            @Param("userId") Long userId,
            @Param("revokedAt") Instant revokedAt,
            @Param("reason") String reason);

    @Modifying
    @Query("""
            delete from RefreshToken token
             where token.expiresAt < :cutoff
                or token.revokedAt < :cutoff
            """)
    int deleteExpiredOrRevokedBefore(@Param("cutoff") Instant cutoff);
}