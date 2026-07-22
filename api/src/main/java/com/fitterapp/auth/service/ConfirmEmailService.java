package com.fitterapp.auth.service;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.auth.entity.EmailVerificationToken;
import com.fitterapp.auth.exception.AccountNotPendingVerificationException;
import com.fitterapp.auth.exception.InvalidVerificationTokenException;
import com.fitterapp.auth.exception.VerificationTokenAlreadyUsedException;
import com.fitterapp.auth.exception.VerificationTokenExpiredException;
import com.fitterapp.auth.repository.EmailVerificationTokenRepository;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserStatus;

@Service
public class ConfirmEmailService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final TokenHasher tokenHasher;
    private final Clock clock;

    public ConfirmEmailService(
            EmailVerificationTokenRepository tokenRepository,
            TokenHasher tokenHasher,
            Clock clock) {
        this.tokenRepository = tokenRepository;
        this.tokenHasher = tokenHasher;
        this.clock = clock;
    }

    @Transactional
    public void confirm(String rawToken) {
        EmailVerificationToken token = tokenRepository
                .findByTokenHash(tokenHasher.hash(rawToken))
                .orElseThrow(InvalidVerificationTokenException::new);
        OffsetDateTime now = OffsetDateTime.now(clock);

        if (token.isUsed()) {
            throw new VerificationTokenAlreadyUsedException();
        }
        if (token.isExpiredAt(now)) {
            throw new VerificationTokenExpiredException();
        }

        User user = token.getUser();
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new AccountNotPendingVerificationException();
        }

        token.markAsUsed(now);
        user.confirmEmail(now);
    }
}
