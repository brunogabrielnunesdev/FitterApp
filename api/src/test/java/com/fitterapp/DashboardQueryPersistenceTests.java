package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.analytics.service.DashboardQueryService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class DashboardQueryPersistenceTests {
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private DashboardQueryService service;

  @Test
  void aggregatesKnownFunnelAndCatalogEventsWithinTheLocalDatePeriod() {
    OffsetDateTime start = OffsetDateTime.parse("2026-08-10T00:00:00-03:00");
    OffsetDateTime end = OffsetDateTime.parse("2026-08-11T00:00:00-03:00");
    UUID userId = insertUser(start);
    UUID profileId = insertProfile(userId, start);

    insertFunnel(userId, null, "ACCOUNT_COMPLETED", start);
    insertFunnel(userId, profileId, "PROFILE_STARTED", start.plusHours(1));
    insertFunnel(userId, profileId, "PROFILE_SUBMITTED", end.minusSeconds(1));
    insertFunnel(userId, null, "ACCOUNT_COMPLETED", start.minusSeconds(1));
    insertFunnel(userId, profileId, "PROFILE_SUBMITTED", end);

    insertRevision(profileId, 1, "APPROVED", null, start.plusHours(2));
    insertRevision(profileId, 2, "REJECTED", "Incomplete data", start.plusHours(4));
    insertRevision(profileId, 3, "APPROVED", null, end);

    insertSearch(start.plusHours(5), true);
    insertSearch(start.plusHours(6), false);
    insertSearch(start.plusHours(7), true);
    insertSearch(end, true);

    insertProfileView(profileId, start.plusHours(8), true);
    insertProfileView(profileId, start.plusHours(9), false);
    insertContact(profileId, start.plusHours(10), true);
    insertContact(profileId, start.plusHours(11), false);
    insertContact(profileId, end.minusSeconds(1), true);

    var dashboard =
        service.query(
            LocalDate.parse("2026-08-10"),
            LocalDate.parse("2026-08-10"),
            "America/Sao_Paulo");

    assertThat(dashboard.period().startInclusive()).isEqualTo(start);
    assertThat(dashboard.period().endExclusive()).isEqualTo(end);
    assertThat(dashboard.funnel().accountsCompleted()).isEqualTo(1);
    assertThat(dashboard.funnel().profilesStarted()).isEqualTo(1);
    assertThat(dashboard.funnel().profilesSubmitted()).isEqualTo(1);
    assertThat(dashboard.funnel().profilesApproved()).isEqualTo(1);
    assertThat(dashboard.funnel().profilesRejected()).isEqualTo(1);
    assertThat(dashboard.searches().raw()).isEqualTo(3);
    assertThat(dashboard.searches().unique()).isEqualTo(2);
    assertThat(dashboard.profileViews().raw()).isEqualTo(2);
    assertThat(dashboard.profileViews().unique()).isEqualTo(1);
    assertThat(dashboard.whatsappContacts().raw()).isEqualTo(3);
    assertThat(dashboard.whatsappContacts().unique()).isEqualTo(2);
  }

  private UUID insertUser(OffsetDateTime createdAt) {
    UUID id = UUID.randomUUID();
    jdbcTemplate.update(
        """
        INSERT INTO users (
            id, full_name, email, phone_number, password_hash, status,
            email_verified_at, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        "Dashboard User",
        "dashboard." + id + "@fitterapp.test",
        "+5544999999901",
        "$2a$10$dashboard.test.hash",
        "ACTIVE",
        createdAt,
        createdAt,
        createdAt);
    return id;
  }

  private UUID insertProfile(UUID userId, OffsetDateTime createdAt) {
    UUID id = UUID.randomUUID();
    jdbcTemplate.update(
        """
        INSERT INTO personal_profiles (
            id, user_id, slug, status, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?)
        """,
        id,
        userId,
        "dashboard-" + id,
        "DRAFT",
        createdAt,
        createdAt);
    return id;
  }

  private void insertFunnel(
      UUID userId, UUID profileId, String type, OffsetDateTime occurredAt) {
    jdbcTemplate.update(
        """
        INSERT INTO funnel_events (
            id, user_id, personal_profile_id, event_type, source, occurred_at
        ) VALUES (?, ?, ?, ?, ?, ?)
        """,
        UUID.randomUUID(),
        userId,
        profileId,
        type,
        "PUBLIC_WEB",
        occurredAt);
  }

  private void insertRevision(
      UUID profileId,
      int version,
      String status,
      String rejectionReason,
      OffsetDateTime reviewedAt) {
    OffsetDateTime submittedAt = reviewedAt.minusMinutes(30);
    jdbcTemplate.update(
        """
        INSERT INTO personal_profile_revisions (
            id, personal_id, version_number, status, requires_review,
            rejection_reason, submitted_at, reviewed_at, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        UUID.randomUUID(),
        profileId,
        version,
        status,
        true,
        rejectionReason,
        submittedAt,
        reviewedAt,
        submittedAt,
        reviewedAt);
  }

  private void insertSearch(OffsetDateTime occurredAt, boolean unique) {
    jdbcTemplate.update(
        """
        INSERT INTO search_events (
            id, source, filters, result_count, occurred_at, unique_event
        ) VALUES (?, ?, ?::jsonb, ?, ?, ?)
        """,
        UUID.randomUUID(),
        "PUBLIC_WEB",
        "{}",
        1,
        occurredAt,
        unique);
  }

  private void insertProfileView(
      UUID profileId, OffsetDateTime occurredAt, boolean unique) {
    jdbcTemplate.update(
        """
        INSERT INTO profile_view_events (
            id, personal_profile_id, source, occurred_at, unique_event
        ) VALUES (?, ?, ?, ?, ?)
        """,
        UUID.randomUUID(),
        profileId,
        "PUBLIC_WEB",
        occurredAt,
        unique);
  }

  private void insertContact(UUID profileId, OffsetDateTime occurredAt, boolean unique) {
    jdbcTemplate.update(
        """
        INSERT INTO contact_events (
            id, personal_profile_id, source, occurred_at, unique_event
        ) VALUES (?, ?, ?, ?, ?)
        """,
        UUID.randomUUID(),
        profileId,
        "PUBLIC_WEB",
        occurredAt,
        unique);
  }
}
