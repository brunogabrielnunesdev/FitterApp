package com.fitterapp.auth.service.password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.auth.entity.PasswordResetToken;
import com.fitterapp.auth.entity.RefreshToken;
import com.fitterapp.auth.exception.InvalidPasswordResetTokenException;
import com.fitterapp.auth.repository.PasswordResetTokenRepository;
import com.fitterapp.auth.repository.RefreshTokenRepository;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.User;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTests {
  @Mock PasswordResetTokenRepository resetTokens;
  @Mock RefreshTokenRepository refreshTokens;
  @Mock TokenHasher hasher;
  @Mock PasswordEncoder encoder;

  @Test
  void changesPasswordConsumesTokenAndRevokesSessions() {
    OffsetDateTime now = OffsetDateTime.parse("2026-08-04T18:00:00Z");
    User user =
        User.pendingRegistration(
            "Bruno", "bruno@fitterapp.com", "+5544999999999", "old", now.minusDays(1));
    PasswordResetToken resetToken =
        PasswordResetToken.issue(user, "hash", now.minusMinutes(5), now.plusMinutes(25));
    RefreshToken refreshToken =
        RefreshToken.issue(
            user, UUID.randomUUID(), "refresh", now.minusHours(1), now.plusDays(1), null, null);
    when(hasher.hash("raw")).thenReturn("hash");
    when(resetTokens.findByTokenHash("hash")).thenReturn(Optional.of(resetToken));
    when(encoder.encode("new-password")).thenReturn("new-hash");
    when(refreshTokens.findAllByUserIdAndRevokedAtIsNull(user.getId()))
        .thenReturn(List.of(refreshToken));
    var service =
        new ResetPasswordService(
            resetTokens,
            refreshTokens,
            hasher,
            encoder,
            Clock.fixed(Instant.from(now), ZoneOffset.UTC));

    service.reset("raw", "new-password");

    assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    assertThat(resetToken.isUsed()).isTrue();
    assertThat(refreshToken.isRevoked()).isTrue();
  }

  @Test
  void rejectsExpiredOrUsedTokens() {
    var now = OffsetDateTime.parse("2026-08-04T18:00:00Z");
    var user =
        User.pendingRegistration(
            "Bruno", "bruno@fitterapp.com", "+5544999999999", "old", now.minusDays(1));
    var expired = PasswordResetToken.issue(user, "expired", now.minusHours(1), now.minusMinutes(1));
    var used = PasswordResetToken.issue(user, "used", now.minusMinutes(5), now.plusMinutes(25));
    used.markAsUsed(now.minusMinutes(1));
    when(hasher.hash("expired-raw")).thenReturn("expired");
    when(hasher.hash("used-raw")).thenReturn("used");
    when(resetTokens.findByTokenHash("expired")).thenReturn(Optional.of(expired));
    when(resetTokens.findByTokenHash("used")).thenReturn(Optional.of(used));
    var service =
        new ResetPasswordService(
            resetTokens,
            refreshTokens,
            hasher,
            encoder,
            Clock.fixed(Instant.from(now), ZoneOffset.UTC));
    assertThatThrownBy(() -> service.reset("expired-raw", "new-password"))
        .isInstanceOf(InvalidPasswordResetTokenException.class);
    assertThatThrownBy(() -> service.reset("used-raw", "new-password"))
        .isInstanceOf(InvalidPasswordResetTokenException.class);
    verify(encoder, never()).encode(anyString());
    assertThat(user.getPasswordHash()).isEqualTo("old");
  }
}
