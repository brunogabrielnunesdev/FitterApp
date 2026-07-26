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
class ModerationSchemaMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsModerationTables() {
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """, String.class);

        assertThat(tables).contains(
                "moderation_reports",
                "moderation_report_evidences",
                "profile_suspensions",
                "reactivation_requests",
                "account_blocks",
                "blacklist_entries",
                "moderation_appeals");
    }

    @Test
    void requiresExactlyOneReportTarget() {
        UUID reporterId = insertUser("reporter-target@fitterapp.test");
        UUID personalId = insertPersonal(reporterId, "report-target");
        UUID academyId = insertAcademy("report_target");

        assertThatThrownBy(() -> insertReport(
                reporterId,
                personalId,
                academyId,
                "HARASSMENT",
                null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void requiresDescriptionForOtherReason() {
        UUID reporterId = insertUser("reporter-other@fitterapp.test");
        UUID personalId = insertPersonal(reporterId, "report-other");

        assertThatThrownBy(() -> insertReport(
                reporterId,
                personalId,
                null,
                "OTHER",
                null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void preventsDuplicatedOpenReportForSameTargetAndReason() {
        UUID reporterId = insertUser("reporter-duplicate@fitterapp.test");
        UUID personalId = insertPersonal(reporterId, "report-duplicate");
        insertReport(reporterId, personalId, null, "FALSE_INFORMATION", null);

        assertThatThrownBy(() -> insertReport(
                reporterId,
                personalId,
                null,
                "FALSE_INFORMATION",
                "Novo detalhe"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void limitsEvidenceToThreePositions() {
        UUID reporterId = insertUser("reporter-evidence@fitterapp.test");
        UUID personalId = insertPersonal(reporterId, "report-evidence");
        UUID reportId = insertReport(
                reporterId,
                personalId,
                null,
                "FRAUD_OR_SCAM",
                null);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO moderation_report_evidences (
                    id, report_id, position, storage_key, created_at
                ) VALUES (?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                reportId,
                4,
                "moderation/reports/%s/%s.webp"
                        .formatted(reportId, UUID.randomUUID()),
                now()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void preventsTwoActiveSuspensionsForSameProfile() {
        UUID adminId = insertUser("admin-suspension@fitterapp.test");
        UUID personalId = insertPersonal(adminId, "suspended-profile");
        insertActiveSuspension(personalId, adminId);

        assertThatThrownBy(() -> insertActiveSuspension(personalId, adminId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void preventsTwoPendingReactivationRequestsForSameSuspension() {
        UUID userId = insertUser("reactivation@fitterapp.test");
        UUID suspensionId = insertActiveSuspension(
                insertPersonal(userId, "reactivation-profile"),
                userId);
        insertReactivationRequest(suspensionId, userId);

        assertThatThrownBy(() -> insertReactivationRequest(suspensionId, userId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void acceptsOnlySha256HashInBlacklist() {
        UUID userId = insertUser("blocked@fitterapp.test");
        UUID blockId = insertAccountBlock(userId);
        OffsetDateTime now = now();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO blacklist_entries (
                    id, account_block_id, identifier_type, identifier_hash,
                    status, expires_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                blockId,
                "EMAIL",
                "email-em-texto-puro",
                "ACTIVE",
                now.plusYears(2),
                now,
                now))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void preventsDuplicatedActiveBlacklistIdentifier() {
        UUID firstUser = insertUser("first-block@fitterapp.test");
        UUID secondUser = insertUser("second-block@fitterapp.test");
        String hash = "a".repeat(64);
        insertBlacklistEntry(insertAccountBlock(firstUser), hash);

        assertThatThrownBy(() ->
                insertBlacklistEntry(insertAccountBlock(secondUser), hash))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID insertReport(
            UUID reporterId,
            UUID personalId,
            UUID academyId,
            String reason,
            String description) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO moderation_reports (
                    id, reporter_user_id, personal_profile_id,
                    academy_profile_id, reason, description, status, priority,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                reporterId,
                personalId,
                academyId,
                reason,
                description,
                "OPEN",
                "NORMAL",
                now,
                now);

        return id;
    }

    private UUID insertActiveSuspension(UUID personalId, UUID adminId) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO profile_suspensions (
                    id, personal_profile_id, suspended_by, reason,
                    previous_status, status, suspended_at,
                    eligible_for_reactivation_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                personalId,
                adminId,
                "Motivo interno da suspensao",
                "APPROVED",
                "ACTIVE",
                now,
                now.plusDays(7),
                now,
                now);

        return id;
    }

    private void insertReactivationRequest(UUID suspensionId, UUID userId) {
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO reactivation_requests (
                    id, suspension_id, requested_by, reason, status,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                suspensionId,
                userId,
                "Perfil corrigido",
                "PENDING",
                now,
                now);
    }

    private UUID insertAccountBlock(UUID userId) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO account_blocks (
                    id, user_id, blocked_by, reason, status, blocked_at,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                userId,
                userId,
                "Fraude confirmada",
                "ACTIVE",
                now,
                now,
                now);

        return id;
    }

    private void insertBlacklistEntry(UUID accountBlockId, String hash) {
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO blacklist_entries (
                    id, account_block_id, identifier_type, identifier_hash,
                    status, expires_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                accountBlockId,
                "EMAIL",
                hash,
                "ACTIVE",
                now.plusYears(2),
                now,
                now);
    }

    private UUID insertUser(String email) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = now();
        String phone = "+55" + String.format(
                "%011d",
                Integer.toUnsignedLong(email.hashCode()));

        jdbcTemplate.update("""
                INSERT INTO users (
                    id, full_name, email, phone_number, password_hash, status,
                    email_verified_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                "Moderation Test User",
                email,
                phone,
                "$2a$10$moderation.test.hash",
                "ACTIVE",
                now,
                now,
                now);

        return id;
    }

    private UUID insertPersonal(UUID userId, String slug) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = now();

        jdbcTemplate.update("""
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

        jdbcTemplate.update("""
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
