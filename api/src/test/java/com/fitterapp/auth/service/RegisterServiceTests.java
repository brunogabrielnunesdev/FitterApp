package com.fitterapp.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fitterapp.auth.entity.EmailVerificationToken;
import com.fitterapp.auth.exception.EmailAlreadyRegisteredException;
import com.fitterapp.auth.exception.RoleNotConfiguredException;
import com.fitterapp.auth.repository.EmailVerificationTokenRepository;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.Role;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.RoleRepository;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTests {

    private static final Instant NOW = Instant.parse("2026-07-22T18:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private EmailVerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private TokenHasher tokenHasher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Role studentRole;

    private RegisterService registerService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        registerService = new RegisterService(
                userRepository,
                roleRepository,
                userRoleRepository,
                verificationTokenRepository,
                passwordEncoder,
                tokenGenerator,
                tokenHasher,
                eventPublisher,
                clock);
    }

    @Test
    void registersPendingStudentAndIssuesVerificationToken() {
        when(userRepository.existsByEmail("student@fitterapp.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("StrongPassword123!")).thenReturn("encoded-password");
        when(tokenGenerator.generate()).thenReturn("raw-verification-token");
        when(tokenHasher.hash("raw-verification-token")).thenReturn("a".repeat(64));

        RegistrationResult result = registerService.register(new RegisterCommand(
                "  Bruno   Gabriel  ",
                "  STUDENT@FITTERAPP.COM ",
                "+5544999999999",
                "StrongPassword123!"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);

        verify(userRepository).save(userCaptor.capture());
        verify(userRoleRepository).save(roleCaptor.capture());
        verify(verificationTokenRepository).save(tokenCaptor.capture());

        User user = userCaptor.getValue();
        assertThat(user.getFullName()).isEqualTo("Bruno Gabriel");
        assertThat(user.getEmail()).isEqualTo("student@fitterapp.com");
        assertThat(user.getPhoneNumber()).isEqualTo("+5544999999999");
        assertThat(user.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(user.getCreatedAt().toInstant()).isEqualTo(NOW);

        UserRole userRole = roleCaptor.getValue();
        assertThat(userRole.getUser()).isSameAs(user);
        assertThat(userRole.getRole()).isSameAs(studentRole);
        assertThat(userRole.getGrantedBy()).isNull();

        EmailVerificationToken token = tokenCaptor.getValue();
        assertThat(token.getUser()).isSameAs(user);
        assertThat(token.getTokenHash()).isEqualTo("a".repeat(64));
        assertThat(token.getExpiresAt().toInstant()).isEqualTo(NOW.plusSeconds(86_400));
        assertThat(token.getUsedAt()).isNull();

        assertThat(result.userId()).isEqualTo(user.getId());
        verify(eventPublisher).publishEvent(new VerificationEmailRequested(
                "student@fitterapp.com",
                "Bruno Gabriel",
                "raw-verification-token"));
    }

    @Test
    void rejectsAlreadyRegisteredEmailBeforeWritingAnything() {
        when(userRepository.existsByEmail("student@fitterapp.com")).thenReturn(true);

        assertThatThrownBy(() -> registerService.register(new RegisterCommand(
                "Bruno Gabriel",
                "STUDENT@FITTERAPP.COM",
                "+5544999999999",
                "StrongPassword123!")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
        verify(verificationTokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void failsWhenStudentRoleIsNotConfigured() {
        when(userRepository.existsByEmail("student@fitterapp.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.STUDENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerService.register(new RegisterCommand(
                "Bruno Gabriel",
                "student@fitterapp.com",
                "+5544999999999",
                "StrongPassword123!")))
                .isInstanceOf(RoleNotConfiguredException.class);

        verify(userRepository, never()).save(any());
        verify(userRoleRepository, never()).save(any());
        verify(verificationTokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
