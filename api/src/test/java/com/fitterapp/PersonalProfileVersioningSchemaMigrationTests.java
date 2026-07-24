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
class PersonalProfileVersioningSchemaMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsVersioningAndCrefTables() {
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """, String.class);

        assertThat(tables).contains(
                "personal_crefs",
                "personal_profile_revisions",
                "personal_revision_modalities",
                "personal_revision_service_modes",
                "personal_revision_service_areas");
    }

    @Test
    void seedsOwnerRole() {
        assertThat(jdbcTemplate.queryForObject(
                "SELECT id FROM roles WHERE name = 'OWNER'",
                Short.class))
                .isEqualTo((short) 4);
    }

    @Test
    void allowsOnlyOneOpenRevisionPerPersonal() {
        UUID personalId = insertProfile("one-open-revision");
        insertDraftRevision(personalId, 1);

        assertThatThrownBy(() -> insertDraftRevision(personalId, 2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsPendingRevisionWithoutSubmissionTimestamp() {
        UUID personalId = insertProfile("invalid-pending-revision");
        OffsetDateTime now = now();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO personal_profile_revisions (
                    id, personal_id, version_number, status, requires_review,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                personalId,
                1,
                "PENDING_REVIEW",
                true,
                now,
                now))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void storesPrivateCrefDocumentForReview() {
        UUID personalId = insertProfile("cref-review");
        UUID crefId = UUID.randomUUID();
        OffsetDateTime now = now();
        String documentKey = "private/crefs/" + personalId + "/" + UUID.randomUUID() + ".webp";

        jdbcTemplate.update("""
                INSERT INTO personal_crefs (
                    id, personal_id, registration_code, document_image_key,
                    status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                crefId,
                personalId,
                "012345-G/PR",
                documentKey,
                "PENDING_REVIEW",
                now,
                now);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT document_image_key FROM personal_crefs WHERE id = ?",
                String.class,
                crefId))
                .isEqualTo(documentKey);
    }

    @Test
    void rejectsPublicPathForCrefDocument() {
        UUID personalId = insertProfile("invalid-cref-path");
        OffsetDateTime now = now();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO personal_crefs (
                    id, personal_id, registration_code, document_image_key,
                    status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                personalId,
                "999999-G/PR",
                "personals/" + personalId + "/" + UUID.randomUUID() + ".webp",
                "PENDING_REVIEW",
                now,
                now))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID insertProfile(String slug) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO personal_profiles (
                    id, slug, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?)
                """,
                id,
                slug,
                "DRAFT",
                now,
                now);

        return id;
    }

    private void insertDraftRevision(UUID personalId, int version) {
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO personal_profile_revisions (
                    id, personal_id, version_number, status, requires_review,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                personalId,
                version,
                "DRAFT",
                true,
                now,
                now);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}
