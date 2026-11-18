package com.fitterapp.auth.service.password;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fitterapp.auth.entity.PasswordResetToken;
import com.fitterapp.auth.repository.PasswordResetTokenRepository;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.repository.UserRepository;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

class RequestPasswordResetServiceTests {
  private final UserRepository users = mock(UserRepository.class);
  private final PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
  private final TokenGenerator generator = mock(TokenGenerator.class);
  private final TokenHasher hasher = mock(TokenHasher.class);
  private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
  private final OffsetDateTime now = OffsetDateTime.parse("2026-08-04T18:00:00Z");

  private RequestPasswordResetService service() {
    return new RequestPasswordResetService(
        users, tokens, generator, hasher, events, Clock.fixed(now.toInstant(), ZoneOffset.UTC));
  }

  @Test
  void createsHashedTokenAndRequestsEmailForExistingUser() {
    var user =
        User.pendingRegistration("Bruno", "user@example.com", "123", "hash", now.minusDays(1));
    when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(generator.generate()).thenReturn("secret-token");
    when(hasher.hash("secret-token")).thenReturn("hashed-token");
    service().request(" User@Example.com ");
    var token = ArgumentCaptor.forClass(PasswordResetToken.class);
    verify(tokens).save(token.capture());
    assertThat(token.getValue().getTokenHash()).isEqualTo("hashed-token");
    assertThat(token.getValue().getExpiresAt()).isEqualTo(now.plusMinutes(30));
    verify(events)
        .publishEvent(new PasswordResetRequested("user@example.com", "Bruno", "secret-token"));
  }

  @Test
  void ignoresUnknownAccount() {
    when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
    service().request("missing@example.com");
    verify(tokens, never()).save(any());
    verifyNoInteractions(events);
  }
}
