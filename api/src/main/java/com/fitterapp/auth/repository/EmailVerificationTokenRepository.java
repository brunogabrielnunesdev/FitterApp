package com.fitterapp.auth.repository;

import java.util.UUID;
import java.util.Optional;
import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fitterapp.auth.entity.EmailVerificationToken;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE EmailVerificationToken token
            SET token.usedAt = :invalidatedAt
            WHERE token.user.id = :userId
              AND token.usedAt IS NULL
            """)
    int invalidateUnusedByUserId(
            @Param("userId") UUID userId,
            @Param("invalidatedAt") OffsetDateTime invalidatedAt);
}
