package com.fitterapp.auth.service;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.auth.entity.EmailVerificationToken;
import com.fitterapp.auth.exception.EmailAlreadyRegisteredException;
import com.fitterapp.auth.exception.RoleNotConfiguredException;
import com.fitterapp.auth.repository.EmailVerificationTokenRepository;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.auth.service.register.RegisterCommand;
import com.fitterapp.auth.service.register.RegisterResult;
import com.fitterapp.user.entity.Role;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.repository.RoleRepository;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;

@Service
public class RegisterService {

    private static final Duration VERIFICATION_TOKEN_DURATION = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public RegisterService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            EmailVerificationTokenRepository verificationTokenRepository,
            PasswordEncoder passwordEncoder,
            TokenGenerator tokenGenerator,
            TokenHasher tokenHasher,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
        this.tokenHasher = tokenHasher;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    public RegisterResult register(RegisterCommand command) {
        String normalizedEmail = normalizeEmail(command.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new RoleNotConfiguredException(RoleName.STUDENT));
        OffsetDateTime now = OffsetDateTime.now(clock);

        User user = User.pendingRegistration(
                normalizeFullName(command.fullName()),
                normalizedEmail,
                command.phoneNumber().trim(),
                passwordEncoder.encode(command.password()),
                now);
        userRepository.save(user);
        userRoleRepository.save(UserRole.grantedBySystem(user, studentRole, now));

        String rawVerificationToken = tokenGenerator.generate();
        EmailVerificationToken verificationToken = EmailVerificationToken.issue(
                user,
                tokenHasher.hash(rawVerificationToken),
                now,
                now.plus(VERIFICATION_TOKEN_DURATION));
        verificationTokenRepository.save(verificationToken);
        eventPublisher.publishEvent(new VerificationEmailRequested(
                user.getEmail(),
                user.getFullName(),
                rawVerificationToken));

        return new RegisterResult(user.getId());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeFullName(String fullName) {
        return fullName.trim().replaceAll("\\s+", " ");
    }
}
