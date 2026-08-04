package com.fitterapp.auth.service.password;

import com.fitterapp.auth.exception.InvalidPasswordResetTokenException;
import com.fitterapp.auth.repository.PasswordResetTokenRepository;
import com.fitterapp.auth.repository.RefreshTokenRepository;
import com.fitterapp.auth.security.TokenHasher;
import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {
  private final PasswordResetTokenRepository tokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenHasher tokenHasher;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;

  @Transactional
  public void reset(String rawToken, String newPassword) {
    var token =
        tokenRepository
            .findByTokenHash(tokenHasher.hash(rawToken))
            .orElseThrow(InvalidPasswordResetTokenException::new);
    OffsetDateTime now = OffsetDateTime.now(clock);
    if (token.isUsed() || token.isExpiredAt(now)) {
      throw new InvalidPasswordResetTokenException();
    }
    token.getUser().changePassword(passwordEncoder.encode(newPassword), now);
    token.markAsUsed(now);
    refreshTokenRepository
        .findAllByUserIdAndRevokedAtIsNull(token.getUser().getId())
        .forEach(refreshToken -> refreshToken.revoke(now));
  }
}
