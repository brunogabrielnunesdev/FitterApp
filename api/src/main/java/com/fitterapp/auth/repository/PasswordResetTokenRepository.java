package com.fitterapp.auth.repository;

import com.fitterapp.auth.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
