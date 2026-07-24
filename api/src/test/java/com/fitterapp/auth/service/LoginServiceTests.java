package com.fitterapp.auth.service;

import com.fitterapp.auth.service.login.LoginCommand;
import com.fitterapp.auth.service.login.LoginResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fitterapp.auth.entity.RefreshToken;
import com.fitterapp.auth.exception.AccountBlockedException;
import com.fitterapp.auth.exception.EmailNotVerifiedException;
import com.fitterapp.auth.exception.InvalidCredentialsException;
import com.fitterapp.auth.repository.RefreshTokenRepository;
import com.fitterapp.auth.security.AccessTokenIssuer;
import com.fitterapp.auth.security.IssuedAccessToken;
import com.fitterapp.auth.security.TokenGenerator;
import com.fitterapp.auth.security.TokenHasher;
import com.fitterapp.user.entity.Role;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;

@ExtendWith(MockitoExtension.class)
class LoginServiceTests {

    private static final Instant NOW = Instant.parse("2026-07-22T22:00:00Z");

    @Mock private UserRepository userRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenGenerator tokenGenerator;
    @Mock private TokenHasher tokenHasher;
    @Mock private AccessTokenIssuer accessTokenIssuer;
    @Mock private UserRole userRole;
    @Mock private Role role;

    private LoginService service;
    private User activeUser;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
        activeUser = User.pendingRegistration(
                "Bruno Gabriel", "bruno@fitterapp.com", "+5544999999999", "password-hash", now.minusDays(1));
        activeUser.confirmEmail(now.minusHours(23));
        service = new LoginService(
                userRepository, userRoleRepository, refreshTokenRepository, passwordEncoder,
                tokenGenerator, tokenHasher, accessTokenIssuer, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void logsInActiveUserAndStoresOnlyRefreshTokenHash() throws Exception {
        OffsetDateTime now = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
        InetAddress ipAddress = InetAddress.getByName("127.0.0.1");
        when(userRepository.findByEmail("bruno@fitterapp.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("correct-password", "password-hash")).thenReturn(true);
        when(userRoleRepository.findAllByUserId(activeUser.getId())).thenReturn(List.of(userRole));
        when(userRole.getRole()).thenReturn(role);
        when(role.getName()).thenReturn(RoleName.STUDENT);
        when(accessTokenIssuer.issue(activeUser.getId(), activeUser.getEmail(), Set.of(RoleName.STUDENT), now))
                .thenReturn(new IssuedAccessToken("access-token", now.plusMinutes(15)));
        when(tokenGenerator.generate()).thenReturn("raw-refresh-token");
        when(tokenHasher.hash("raw-refresh-token")).thenReturn("c".repeat(64));

        LoginResult result = service.login(new LoginCommand(
                " BRUNO@FITTERAPP.COM ", "correct-password", "FitterApp/1.0", ipAddress));

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken storedToken = tokenCaptor.getValue();
        assertThat(storedToken.getUser()).isSameAs(activeUser);
        assertThat(storedToken.getTokenHash()).isEqualTo("c".repeat(64));
        assertThat(storedToken.getCreatedAt()).isEqualTo(now);
        assertThat(storedToken.getExpiresAt()).isEqualTo(now.plusDays(30));
        assertThat(storedToken.getUserAgent()).isEqualTo("FitterApp/1.0");
        assertThat(storedToken.getIpAddress()).isEqualTo(ipAddress);
        assertThat(result).isEqualTo(new LoginResult("access-token", "raw-refresh-token", 900));
    }

    @Test
    void rejectsUnknownEmailWithGenericCredentialsError() {
        when(userRepository.findByEmail("unknown@fitterapp.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(command("unknown@fitterapp.com", "password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rejectsWrongPasswordBeforeRevealingAccountStatus() {
        when(userRepository.findByEmail("bruno@fitterapp.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong-password", "password-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(command("bruno@fitterapp.com", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rejectsPendingAccountAfterValidPassword() {
        OffsetDateTime now = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
        User pendingUser = User.pendingRegistration(
                "Pending User", "pending@fitterapp.com", "+5544999999998", "pending-hash", now);
        when(userRepository.findByEmail("pending@fitterapp.com")).thenReturn(Optional.of(pendingUser));
        when(passwordEncoder.matches("correct-password", "pending-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(command("pending@fitterapp.com", "correct-password")))
                .isInstanceOf(EmailNotVerifiedException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rejectsBlockedAccountAfterValidPassword() {
        activeUser.block(OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
        when(userRepository.findByEmail("bruno@fitterapp.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("correct-password", "password-hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login(command("bruno@fitterapp.com", "correct-password")))
                .isInstanceOf(AccountBlockedException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    private LoginCommand command(String email, String password) {
        return new LoginCommand(email, password, null, null);
    }
}
