package com.fitterapp.auth.service.emailconfirm;

import lombok.RequiredArgsConstructor;

import com.fitterapp.auth.service.verification.VerificationEmailRequested;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.auth.entity.EmailVerificationToken;
import com.fitterapp.auth.repository.EmailVerificationTokenRepository;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ResendConfirmationService {

    private static final Duration TOKEN_DURATION = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final TokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public void resend(String email) {
        userRepository.findByEmail(normalizeEmail(email))
                .filter(user -> user.getStatus() == UserStatus.PENDING_VERIFICATION)
                .ifPresent(this::replaceTokenAndSend);
    }

    private void replaceTokenAndSend(User user) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        tokenRepository.invalidateUnusedByUserId(user.getId(), now);

        String rawToken = tokenGenerator.generate();
        tokenRepository.save(EmailVerificationToken.issue(
                user,
                tokenHasher.hash(rawToken),
                now,
                now.plus(TOKEN_DURATION)));
        eventPublisher.publishEvent(new VerificationEmailRequested(
                user.getEmail(),
                user.getFullName(),
                rawToken));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
