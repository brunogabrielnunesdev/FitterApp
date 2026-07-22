package com.fitterapp.auth.service;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.auth.entity.RefreshToken;
import com.fitterapp.auth.exception.AccountBlockedException;
import com.fitterapp.auth.exception.EmailNotVerifiedException;
import com.fitterapp.auth.exception.InvalidCredentialsException;
import com.fitterapp.auth.repository.RefreshTokenRepository;
import com.fitterapp.auth.security.AccessTokenIssuer;
import com.fitterapp.auth.security.IssuedAccessToken;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;

@Service
public class LoginService {

    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(30);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final Clock clock;

    public LoginService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            TokenGenerator tokenGenerator,
            TokenHasher tokenHasher,
            AccessTokenIssuer accessTokenIssuer,
            Clock clock) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
        this.tokenHasher = tokenHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.clock = clock;
    }

    @Transactional
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmail(normalizeEmail(command.email()))
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        validateStatus(user.getStatus());

        OffsetDateTime now = OffsetDateTime.now(clock);
        Set<RoleName> roles = userRoleRepository.findAllByUserId(user.getId()).stream()
                .map(userRole -> userRole.getRole().getName())
                .collect(Collectors.toUnmodifiableSet());
        IssuedAccessToken accessToken = accessTokenIssuer.issue(
                user.getId(), user.getEmail(), roles, now);

        String rawRefreshToken = tokenGenerator.generate();
        refreshTokenRepository.save(RefreshToken.issue(
                user,
                UUID.randomUUID(),
                tokenHasher.hash(rawRefreshToken),
                now,
                now.plus(REFRESH_TOKEN_DURATION),
                command.userAgent(),
                command.ipAddress()));

        long expiresIn = now.until(accessToken.expiresAt(), ChronoUnit.SECONDS);
        return new LoginResult(accessToken.value(), rawRefreshToken, expiresIn);
    }

    private void validateStatus(UserStatus status) {
        if (status == UserStatus.PENDING_VERIFICATION) {
            throw new EmailNotVerifiedException();
        }
        if (status == UserStatus.BLOCKED) {
            throw new AccountBlockedException();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
