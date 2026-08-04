package com.fitterapp.auth.service.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.auth.entity.RefreshToken;
import com.fitterapp.auth.exception.InvalidRefreshTokenException;
import com.fitterapp.auth.repository.RefreshTokenRepository;
import com.fitterapp.auth.security.AccessTokenIssuer;
import com.fitterapp.auth.security.IssuedAccessToken;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.repository.UserRoleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshSessionServiceTests {
  private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-08-04T18:00:00Z");
  @Mock RefreshTokenRepository tokens;
  @Mock UserRoleRepository roles;
  @Mock TokenGenerator generator;
  @Mock TokenHasher hasher;
  @Mock AccessTokenIssuer accessTokens;
  RefreshSessionService service;

  @BeforeEach
  void setUp() {
    service =
        new RefreshSessionService(
            tokens,
            roles,
            generator,
            hasher,
            accessTokens,
            Clock.fixed(Instant.from(NOW), ZoneOffset.UTC));
  }

  @Test
  void rotatesRefreshTokenAndIssuesNewAccessToken() {
    User user = activeUser();
    RefreshToken current =
        RefreshToken.issue(user, UUID.randomUUID(), "hash", NOW.minusDays(1), NOW.plusDays(1), null, null);
    when(hasher.hash("raw")).thenReturn("hash");
    when(hasher.hash("replacement")).thenReturn("replacement-hash");
    when(tokens.findByTokenHash("hash")).thenReturn(Optional.of(current));
    when(roles.findAllByUserId(any())).thenReturn(List.of());
    when(generator.generate()).thenReturn("replacement");
    when(accessTokens.issue(any(), any(), any(), any()))
        .thenReturn(new IssuedAccessToken("access", NOW.plusMinutes(15)));

    var result = service.refresh("raw", "agent", null);

    assertThat(result.accessToken()).isEqualTo("access");
    assertThat(result.refreshToken()).isEqualTo("replacement");
    assertThat(current.isRevoked()).isTrue();
    assertThat(current.getReplacedBy()).isNotNull();
    verify(tokens).save(any(RefreshToken.class));
  }

  @Test
  void rejectsReuseAndRevokesWholeFamily() {
    User user = activeUser();
    UUID family = UUID.randomUUID();
    RefreshToken reused =
        RefreshToken.issue(user, family, "hash", NOW.minusDays(2), NOW.plusDays(1), null, null);
    RefreshToken sibling =
        RefreshToken.issue(user, family, "other", NOW.minusDays(1), NOW.plusDays(2), null, null);
    reused.revoke(NOW.minusHours(1));
    when(hasher.hash("raw")).thenReturn("hash");
    when(tokens.findByTokenHash("hash")).thenReturn(Optional.of(reused));
    when(tokens.findAllByFamilyId(family)).thenReturn(List.of(reused, sibling));

    assertThatThrownBy(() -> service.refresh("raw", null, null))
        .isInstanceOf(InvalidRefreshTokenException.class);
    assertThat(sibling.isRevoked()).isTrue();
  }

  private User activeUser() {
    User user =
        User.pendingRegistration("Bruno", "bruno@fitterapp.com", "+5544999999999", "hash", NOW.minusDays(3));
    user.confirmEmail(NOW.minusDays(2));
    return user;
  }
}
