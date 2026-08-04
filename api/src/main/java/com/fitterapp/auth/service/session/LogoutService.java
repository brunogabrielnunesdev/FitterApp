package com.fitterapp.auth.service.session;

import com.fitterapp.auth.repository.RefreshTokenRepository;
import com.fitterapp.auth.security.TokenHasher;
import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenHasher tokenHasher;
  private final Clock clock;

  @Transactional
  public void logout(String rawToken) {
    var token = refreshTokenRepository.findByTokenHash(tokenHasher.hash(rawToken));
    if (token.isEmpty()) return;
    OffsetDateTime now = OffsetDateTime.now(clock);
    refreshTokenRepository
        .findAllByFamilyId(token.get().getFamilyId())
        .forEach(item -> item.revoke(now));
  }
}
