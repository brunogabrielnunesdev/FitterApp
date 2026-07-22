package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fitterapp.auth.entity.EmailVerificationToken;
import com.fitterapp.auth.entity.PasswordResetToken;
import com.fitterapp.auth.entity.RefreshToken;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.entity.UserRoleId;
import com.fitterapp.user.entity.UserStatus;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthenticationPersistenceTests {

    private static final String FIRST_HASH = "a".repeat(64);
    private static final String SECOND_HASH = "b".repeat(64);
    private static final String THIRD_HASH = "c".repeat(64);
    private static final String FOURTH_HASH = "d".repeat(64);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void loadsUserRoleWithItsCompositeKeyAndRelationships() {
        UUID adminId = insertUser("admin@fitterapp.com", "+5544999999901");
        UUID studentId = insertUser("student@fitterapp.com", "+5544999999902");
        OffsetDateTime grantedAt = now();

        jdbcTemplate.update("""
                INSERT INTO user_roles (user_id, role_id, granted_at, granted_by)
                VALUES (?, ?, ?, ?)
                """, studentId, (short) 1, grantedAt, adminId);

        entityManager.clear();
        UserRole userRole = entityManager.find(
                UserRole.class,
                new UserRoleId(studentId, (short) 1));

        assertThat(userRole).isNotNull();
        assertThat(userRole.getUser().getEmail()).isEqualTo("student@fitterapp.com");
        assertThat(userRole.getRole().getName()).isEqualTo(RoleName.STUDENT);
        assertThat(userRole.getGrantedBy().getEmail()).isEqualTo("admin@fitterapp.com");
        assertThat(userRole.getGrantedAt()).isEqualTo(grantedAt);
    }

    @Test
    void loadsRefreshTokenRotationAndPostgresIpAddress() {
        UUID userId = insertUser("session@fitterapp.com", "+5544999999903");
        UUID familyId = UUID.randomUUID();
        UUID replacementId = UUID.randomUUID();
        UUID originalId = UUID.randomUUID();
        OffsetDateTime createdAt = now();

        insertRefreshToken(
                replacementId,
                userId,
                familyId,
                SECOND_HASH,
                createdAt.plusMinutes(1),
                null,
                null);
        insertRefreshToken(
                originalId,
                userId,
                familyId,
                FIRST_HASH,
                createdAt,
                createdAt.plusSeconds(30),
                replacementId);

        entityManager.clear();
        RefreshToken original = entityManager.find(RefreshToken.class, originalId);

        assertThat(original).isNotNull();
        assertThat(original.getUser().getId()).isEqualTo(userId);
        assertThat(original.getFamilyId()).isEqualTo(familyId);
        assertThat(original.getTokenHash()).isEqualTo(FIRST_HASH);
        assertThat(original.getReplacedBy().getId()).isEqualTo(replacementId);
        assertThat(original.getIpAddress().getHostAddress()).isEqualTo("127.0.0.1");
        assertThat(original.getRevokedAt()).isEqualTo(createdAt.plusSeconds(30));
    }

    @Test
    void loadsEmailVerificationAndPasswordResetTokens() {
        UUID userId = insertUser("tokens@fitterapp.com", "+5544999999904");
        UUID verificationId = UUID.randomUUID();
        UUID resetId = UUID.randomUUID();
        OffsetDateTime createdAt = now();

        jdbcTemplate.update("""
                INSERT INTO email_verification_tokens (
                    id, user_id, token_hash, expires_at, used_at, created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                verificationId,
                userId,
                THIRD_HASH,
                createdAt.plusHours(24),
                createdAt.plusMinutes(5),
                createdAt);

        jdbcTemplate.update("""
                INSERT INTO password_reset_tokens (
                    id, user_id, token_hash, expires_at, used_at, created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                resetId,
                userId,
                FOURTH_HASH,
                createdAt.plusMinutes(30),
                null,
                createdAt);

        entityManager.clear();
        EmailVerificationToken verification = entityManager.find(
                EmailVerificationToken.class,
                verificationId);
        PasswordResetToken reset = entityManager.find(
                PasswordResetToken.class,
                resetId);

        assertThat(verification.getUser().getId()).isEqualTo(userId);
        assertThat(verification.getTokenHash()).isEqualTo(THIRD_HASH);
        assertThat(verification.getUsedAt()).isEqualTo(createdAt.plusMinutes(5));
        assertThat(reset.getUser().getId()).isEqualTo(userId);
        assertThat(reset.getTokenHash()).isEqualTo(FOURTH_HASH);
        assertThat(reset.getUsedAt()).isNull();
    }

    private UUID insertUser(String email, String phoneNumber) {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = now();

        jdbcTemplate.update("""
                INSERT INTO users (
                    id, full_name, email, phone_number, password_hash,
                    status, email_verified_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                "Persistence Test",
                email,
                phoneNumber,
                "test-password-hash",
                UserStatus.ACTIVE.name(),
                timestamp,
                timestamp,
                timestamp);

        return id;
    }

    private void insertRefreshToken(
            UUID id,
            UUID userId,
            UUID familyId,
            String tokenHash,
            OffsetDateTime createdAt,
            OffsetDateTime revokedAt,
            UUID replacedById) {
        jdbcTemplate.update("""
                INSERT INTO refresh_tokens (
                    id, user_id, family_id, token_hash, expires_at, created_at,
                    last_used_at, revoked_at, replaced_by_id, user_agent, ip_address
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS inet))
                """,
                id,
                userId,
                familyId,
                tokenHash,
                createdAt.plusDays(30),
                createdAt,
                null,
                revokedAt,
                replacedById,
                "FitterApp persistence test",
                "127.0.0.1");
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}
