package com.fitterapp.auth.repository;

import com.fitterapp.auth.entity.EmailVerificationToken;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationTokenRepository
    extends JpaRepository<EmailVerificationToken, UUID> {

  Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
            UPDATE EmailVerificationToken token
            SET token.usedAt = :invalidatedAt
            WHERE token.user.id = :userId
              AND token.usedAt IS NULL
            """)
  int invalidateUnusedByUserId(
      @Param("userId") UUID userId, @Param("invalidatedAt") OffsetDateTime invalidatedAt);
}
