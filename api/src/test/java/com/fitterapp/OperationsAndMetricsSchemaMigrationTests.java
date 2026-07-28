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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class OperationsAndMetricsSchemaMigrationTests {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void createsOperationsAndMetricsTables() {
    List<String> tables =
        jdbcTemplate.queryForList(
            """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """,
            String.class);

    assertThat(tables)
        .contains(
            "admin_audit_logs",
            "search_events",
            "profile_view_events",
            "contact_events",
            "app_access_events");
  }

  @Test
  void preventsAuditLogUpdates() {
    UUID actorId = insertUser("audit-actor@fitterapp.test");
    UUID logId = insertAuditLog(actorId);

    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    """
                UPDATE admin_audit_logs
                SET action = ?
                WHERE id = ?
                """,
                    "ACCOUNT_BLOCK_REVOKED",
                    logId))
        .isInstanceOf(DataAccessException.class)
        .hasMessageContaining("admin_audit_logs are immutable");
  }

  @Test
  void preventsTwoTargetsInProfileViewEvent() {
    UUID userId = insertUser("event-user@fitterapp.test");
    UUID personalId = insertPersonal(userId, "metrics-personal");
    UUID academyId = insertAcademy("metrics_academy");
    OffsetDateTime now = now();

    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    """
                INSERT INTO profile_view_events (
                    id, viewer_user_id, personal_profile_id, academy_profile_id,
                    source, occurred_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                    UUID.randomUUID(),
                    userId,
                    personalId,
                    academyId,
                    "MOBILE_APP",
                    now))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void requiresTargetForContactEvent() {
    UUID userId = insertUser("contact-without-target@fitterapp.test");

    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    """
                INSERT INTO contact_events (
                    id, user_id, source, occurred_at
                ) VALUES (?, ?, ?, ?)
                """,
                    UUID.randomUUID(),
                    userId,
                    "MOBILE_APP",
                    now()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void rejectsInvalidAnalyticsPayloads() {
    OffsetDateTime now = now();

    assertThatThrownBy(
            () ->
                jdbcTemplate.update(
                    """
                INSERT INTO search_events (
                    id, source, filters, result_count, occurred_at
                ) VALUES (?, ?, ?::jsonb, ?, ?)
                """,
                    UUID.randomUUID(),
                    "MOBILE_APP",
                    "[]",
                    -1,
                    now))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void acceptsAnonymizedWhatsappContactEvent() {
    UUID ownerId = insertUser("contact-owner@fitterapp.test");
    UUID personalId = insertPersonal(ownerId, "contact-personal");
    OffsetDateTime now = now();

    jdbcTemplate.update(
        """
                INSERT INTO contact_events (
                    id, personal_profile_id, source, city, occurred_at
                ) VALUES (?, ?, ?, ?, ?)
                """,
        UUID.randomUUID(),
        personalId,
        "MOBILE_APP",
        "Umuarama",
        now);

    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM contact_events WHERE personal_profile_id = ?",
            Integer.class,
            personalId);

    assertThat(count).isEqualTo(1);
  }

  private UUID insertAuditLog(UUID actorId) {
    UUID id = UUID.randomUUID();

    jdbcTemplate.update(
        """
                INSERT INTO admin_audit_logs (
                    id, actor_user_id, action, target_type, target_id,
                    reason, previous_state, new_state, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)
                """,
        id,
        actorId,
        "ACCOUNT_BLOCKED",
        "USER",
        UUID.randomUUID(),
        "Fraude confirmada",
        "{\"status\":\"ACTIVE\"}",
        "{\"status\":\"BLOCKED\"}",
        now());

    return id;
  }

  private UUID insertUser(String email) {
    UUID id = UUID.randomUUID();
    OffsetDateTime now = now();

    jdbcTemplate.update(
        """
                INSERT INTO users (
                    id, full_name, email, phone_number, password_hash, status,
                    email_verified_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
        id,
        "Operations Test User",
        email,
        "+55" + String.format("%011d", Integer.toUnsignedLong(email.hashCode())),
        "$2a$10$operations.test.hash",
        "ACTIVE",
        now,
        now,
        now);

    return id;
  }

  private UUID insertPersonal(UUID userId, String slug) {
    UUID id = UUID.randomUUID();
    OffsetDateTime now = now();

    jdbcTemplate.update(
        """
                INSERT INTO personal_profiles (
                    id, user_id, slug, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
        id,
        userId,
        slug,
        "DRAFT",
        now,
        now);

    return id;
  }

  private UUID insertAcademy(String handle) {
    UUID id = UUID.randomUUID();
    OffsetDateTime now = now();

    jdbcTemplate.update(
        """
                INSERT INTO academy_profiles (
                    id, handle, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?)
                """,
        id,
        handle,
        "DRAFT",
        now,
        now);

    return id;
  }

  private OffsetDateTime now() {
    return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
  }
}
