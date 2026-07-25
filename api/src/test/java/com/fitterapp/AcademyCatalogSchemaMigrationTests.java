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
class AcademyCatalogSchemaMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsAcademyCatalogTables() {
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """, String.class);

        assertThat(tables).contains(
                "academy_profiles",
                "academy_cnpjs",
                "academy_profile_revisions",
                "academy_revision_modalities",
                "academy_members",
                "academy_personal_partnerships");
    }

    @Test
    void allowsOnlyOneOpenRevisionPerAcademy() {
        UUID academyId = insertAcademy("studio_alpha");
        insertDraftRevision(academyId, 1);

        assertThatThrownBy(() -> insertDraftRevision(academyId, 2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void requiresCompleteAddressWhenAddressIsInformed() {
        UUID academyId = insertAcademy("endereco_incompleto");
        OffsetDateTime now = now();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO academy_profile_revisions (
                    id, academy_id, version_number, street, city,
                    status, requires_review, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                academyId,
                1,
                "Avenida Brasil",
                "Umuarama",
                "DRAFT",
                true,
                now,
                now))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicatedCnpjAcrossAcademies() {
        UUID firstAcademy = insertAcademy("primeira_academia");
        UUID secondAcademy = insertAcademy("segunda_academia");
        insertPendingCnpj(firstAcademy, "12345678000190");

        assertThatThrownBy(() ->
                insertPendingCnpj(secondAcademy, "12345678000190"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsOnlyOneActiveOwnerPerAcademy() {
        UUID academyId = insertAcademy("academia_com_dono");
        UUID firstUser = insertUser("owner-one@fitterapp.test");
        UUID secondUser = insertUser("owner-two@fitterapp.test");
        insertMember(academyId, firstUser, "OWNER");

        assertThatThrownBy(() ->
                insertMember(academyId, secondUser, "OWNER"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void preventsDuplicatedOpenPartnership() {
        UUID academyId = insertAcademy("parceria_ativa");
        UUID userId = insertUser("partnership@fitterapp.test");
        UUID personalId = insertPersonal(userId, "personal-parceria");
        insertPendingPartnership(academyId, personalId, userId);

        assertThatThrownBy(() ->
                insertPendingPartnership(academyId, personalId, userId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsAcceptedPartnershipWithoutResponder() {
        UUID academyId = insertAcademy("parceria_invalida");
        UUID userId = insertUser("invalid-partnership@fitterapp.test");
        UUID personalId = insertPersonal(userId, "personal-invalido");
        OffsetDateTime now = now();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO academy_personal_partnerships (
                    id, academy_id, personal_id, initiated_by, requested_by,
                    status, requested_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                academyId,
                personalId,
                "ACADEMY",
                userId,
                "ACCEPTED",
                now,
                now,
                now))
                .isInstanceOf(DataIntegrityViolationException.class);
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

    private void insertDraftRevision(UUID academyId, int version) {
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO academy_profile_revisions (
                    id, academy_id, version_number, status, requires_review,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                academyId,
                version,
                "DRAFT",
                true,
                now,
                now);
    }

    private void insertPendingCnpj(UUID academyId, String cnpj) {
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO academy_cnpjs (
                    id, academy_id, registration_number, status,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                academyId,
                cnpj,
                "PENDING_REVIEW",
                now,
                now);
    }

    private UUID insertUser(String email) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO users (
                    id, full_name, email, phone_number, password_hash, status,
                    email_verified_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                "Schema Test User",
                email,
                "+55" + String.format("%011d", Integer.toUnsignedLong(email.hashCode())),
                "$2a$10$schema.test.hash",
                "ACTIVE",
                now,
                now,
                now);

        return id;
    }

    private void insertMember(UUID academyId, UUID userId, String role) {
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO academy_members (
                    academy_id, user_id, member_role, status, joined_at,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                academyId,
                userId,
                role,
                "ACTIVE",
                now,
                now,
                now);
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

    private void insertPendingPartnership(
            UUID academyId,
            UUID personalId,
            UUID requestedBy) {
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO academy_personal_partnerships (
                    id, academy_id, personal_id, initiated_by, requested_by,
                    status, requested_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                academyId,
                personalId,
                "ACADEMY",
                requestedBy,
                "PENDING",
                now,
                now,
                now);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}
