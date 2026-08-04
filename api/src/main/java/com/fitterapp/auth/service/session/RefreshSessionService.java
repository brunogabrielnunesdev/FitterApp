package com.fitterapp.auth.service.session;

import com.fitterapp.auth.entity.RefreshToken;
import com.fitterapp.auth.exception.AccountBlockedException;
import com.fitterapp.auth.exception.InvalidRefreshTokenException;
import com.fitterapp.auth.repository.RefreshTokenRepository;
import com.fitterapp.auth.security.AccessTokenIssuer;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.UserRoleRepository;
import java.net.InetAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshSessionService {
  private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(30);

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRoleRepository userRoleRepository;
  private final TokenGenerator tokenGenerator;
  private final TokenHasher tokenHasher;
  private final AccessTokenIssuer accessTokenIssuer;
  private final Clock clock;

  @Transactional
  public RefreshSessionResult refresh(String rawToken, String userAgent, InetAddress ipAddress) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    RefreshToken current =
        refreshTokenRepository
            .findByTokenHash(tokenHasher.hash(rawToken))
            .orElseThrow(InvalidRefreshTokenException::new);

    if (current.isRevoked()) {
      revokeFamily(current, now);
      throw new InvalidRefreshTokenException();
    }
    if (current.isExpiredAt(now)) {
      current.revoke(now);
      throw new InvalidRefreshTokenException();
    }
    if (current.getUser().getStatus() == UserStatus.BLOCKED) {
      revokeFamily(current, now);
      throw new AccountBlockedException();
    }

    Set<RoleName> roles =
        userRoleRepository.findAllByUserId(current.getUser().getId()).stream()
            .map(userRole -> userRole.getRole().getName())
            .collect(Collectors.toUnmodifiableSet());
    var accessToken =
        accessTokenIssuer.issue(
            current.getUser().getId(), current.getUser().getEmail(), roles, now);
    String rawReplacement = tokenGenerator.generate();
    RefreshToken replacement =
        RefreshToken.issue(
            current.getUser(),
            current.getFamilyId(),
            tokenHasher.hash(rawReplacement),
            now,
            now.plus(REFRESH_TOKEN_DURATION),
            userAgent,
            ipAddress);
    refreshTokenRepository.save(replacement);
    current.rotateTo(replacement, now);
    return new RefreshSessionResult(
        accessToken.value(),
        rawReplacement,
        now.until(accessToken.expiresAt(), ChronoUnit.SECONDS));
  }

  private void revokeFamily(RefreshToken token, OffsetDateTime timestamp) {
    refreshTokenRepository.findAllByFamilyId(token.getFamilyId()).forEach(item -> item.revoke(timestamp));
  }
}
