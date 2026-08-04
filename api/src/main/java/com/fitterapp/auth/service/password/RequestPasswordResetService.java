package com.fitterapp.auth.service.password;

import com.fitterapp.auth.entity.PasswordResetToken;
import com.fitterapp.auth.repository.PasswordResetTokenRepository;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestPasswordResetService {
  private static final Duration TOKEN_DURATION = Duration.ofMinutes(30);

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final TokenGenerator tokenGenerator;
  private final TokenHasher tokenHasher;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;

  @Transactional
  public void request(String email) {
    var user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT));
    if (user.isEmpty() || user.get().getStatus() == UserStatus.BLOCKED) return;
    OffsetDateTime now = OffsetDateTime.now(clock);
    String rawToken = tokenGenerator.generate();
    tokenRepository.save(
        PasswordResetToken.issue(
            user.get(), tokenHasher.hash(rawToken), now, now.plus(TOKEN_DURATION)));
    eventPublisher.publishEvent(
        new PasswordResetRequested(user.get().getEmail(), user.get().getFullName(), rawToken));
  }
}
