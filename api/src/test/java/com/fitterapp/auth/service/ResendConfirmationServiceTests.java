package com.fitterapp.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.fitterapp.auth.repository.EmailVerificationTokenRepository;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ResendConfirmationServiceTests {

    private static final Instant NOW = Instant.parse("2026-07-22T18:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private TokenHasher tokenHasher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ResendConfirmationService service;

    @BeforeEach
    void setUp() {
        service = new ResendConfirmationService(
                userRepository,
                tokenRepository,
                tokenGenerator,
                tokenHasher,
                eventPublisher,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void invalidatesPreviousTokenAndSendsANewOne() {
        OffsetDateTime createdAt = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
        User user = User.pendingRegistration(
                "Bruno Gabriel",
                "bruno@fitterapp.com",
                "+5544999999999",
                "password-hash",
                createdAt.minusHours(1));
        when(userRepository.findByEmail("bruno@fitterapp.com")).thenReturn(Optional.of(user));
        when(tokenGenerator.generate()).thenReturn("new-raw-token");
        when(tokenHasher.hash("new-raw-token")).thenReturn("b".repeat(64));

        service.resend("  BRUNO@FITTERAPP.COM ");

        verify(tokenRepository).invalidateUnusedByUserId(user.getId(), createdAt);
        verify(tokenRepository).save(any());
        verify(eventPublisher).publishEvent(new VerificationEmailRequested(
                "bruno@fitterapp.com",
                "Bruno Gabriel",
                "new-raw-token"));
    }

    @Test
    void keepsNeutralBehaviorForUnknownEmail() {
        when(userRepository.findByEmail("unknown@fitterapp.com")).thenReturn(Optional.empty());

        service.resend("unknown@fitterapp.com");

        verify(tokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
