package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class AuthSchemaMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsAllAuthenticationTables() {
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """, String.class);

        assertThat(tables).contains(
                "users",
                "roles",
                "user_roles",
                "refresh_tokens",
                "email_verification_tokens",
                "password_reset_tokens");
    }

    @Test
    void seedsTheGlobalRoles() {
        List<String> roles = jdbcTemplate.queryForList(
                "SELECT name FROM roles ORDER BY id",
                String.class);

        assertThat(roles).containsExactly("STUDENT", "PERSONAL", "ADMIN");
    }

    @Test
    void rejectsDuplicatedEmail() {
        insertUser(UUID.randomUUID(), "student@fitterapp.com", "ACTIVE");

        assertThatThrownBy(() ->
                insertUser(UUID.randomUUID(), "student@fitterapp.com", "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsUnsupportedUserStatus() {
        assertThatThrownBy(() ->
                insertUser(UUID.randomUUID(), "status@fitterapp.com", "UNKNOWN"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertUser(UUID id, String email, String status) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        jdbcTemplate.update("""
                INSERT INTO users (
                    id, full_name, email, phone_number, password_hash,
                    status, email_verified_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                "Test User",
                email,
                "+5544999999999",
                "test-password-hash",
                status,
                now,
                now,
                now);
    }
}
