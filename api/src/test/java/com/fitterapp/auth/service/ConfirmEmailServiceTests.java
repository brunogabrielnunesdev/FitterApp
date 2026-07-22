package com.fitterapp.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.fitterapp.auth.entity.EmailVerificationToken;
import com.fitterapp.auth.exception.InvalidVerificationTokenException;
import com.fitterapp.auth.exception.VerificationTokenAlreadyUsedException;
import com.fitterapp.auth.exception.VerificationTokenExpiredException;
import com.fitterapp.auth.repository.EmailVerificationTokenRepository;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserStatus;

@ExtendWith(MockitoExtension.class)
class ConfirmEmailServiceTests {

    private static final Instant NOW = Instant.parse("2026-07-22T18:00:00Z");
    private static final String RAW_TOKEN = "raw-token";
    private static final String TOKEN_HASH = "a".repeat(64);

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private TokenHasher tokenHasher;

    private ConfirmEmailService service;

    @BeforeEach
    void setUp() {
        service = new ConfirmEmailService(
                tokenRepository,
                tokenHasher,
                Clock.fixed(NOW, ZoneOffset.UTC));
        when(tokenHasher.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
    }

    @Test
    void confirmsPendingAccountWithValidToken() {
        User user = pendingUser();
        EmailVerificationToken token = token(user, now().minusHours(1), now().plusHours(23));
        when(tokenRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(token));

        service.confirm(RAW_TOKEN);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmailVerifiedAt().toInstant()).isEqualTo(NOW);
        assertThat(token.getUsedAt().toInstant()).isEqualTo(NOW);
    }

    @Test
    void rejectsUnknownToken() {
        when(tokenRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm(RAW_TOKEN))
                .isInstanceOf(InvalidVerificationTokenException.class);
    }

    @Test
    void rejectsExpiredToken() {
        EmailVerificationToken token = token(
                pendingUser(),
                now().minusDays(2),
                now().minusDays(1));
        when(tokenRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirm(RAW_TOKEN))
                .isInstanceOf(VerificationTokenExpiredException.class);
    }

    @Test
    void rejectsAlreadyUsedToken() {
        EmailVerificationToken token = token(
                pendingUser(),
                now().minusHours(1),
                now().plusHours(23));
        token.markAsUsed(now().minusMinutes(30));
        when(tokenRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirm(RAW_TOKEN))
                .isInstanceOf(VerificationTokenAlreadyUsedException.class);
    }

    private User pendingUser() {
        return User.pendingRegistration(
                "Bruno Gabriel",
                "bruno@fitterapp.com",
                "+5544999999999",
                "password-hash",
                now().minusHours(1));
    }

    private EmailVerificationToken token(
            User user,
            OffsetDateTime createdAt,
            OffsetDateTime expiresAt) {
        return EmailVerificationToken.issue(user, TOKEN_HASH, createdAt, expiresAt);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
    }
}
