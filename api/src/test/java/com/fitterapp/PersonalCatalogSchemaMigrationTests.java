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
class PersonalCatalogSchemaMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createsAllPersonalCatalogTables() {
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """, String.class);

        assertThat(tables).contains(
                "personal_profiles",
                "modalities",
                "personal_modalities",
                "personal_service_modes",
                "personal_service_areas");
    }

    @Test
    void seedsTheInitialModalities() {
        List<String> modalities = jdbcTemplate.queryForList(
                "SELECT slug FROM modalities ORDER BY id",
                String.class);

        assertThat(modalities).containsExactly(
                "musculacao",
                "emagrecimento",
                "hipertrofia",
                "treinamento-funcional",
                "corrida",
                "mobilidade",
                "terceira-idade",
                "preparacao-fisica");
    }

    @Test
    void allowsAProfileWithoutLinkedUser() {
        UUID profileId = UUID.randomUUID();

        insertDraftProfile(profileId, "personal-sem-conta");

        assertThat(jdbcTemplate.queryForObject(
                "SELECT user_id FROM personal_profiles WHERE id = ?",
                UUID.class,
                profileId))
                .isNull();
    }

    @Test
    void rejectsPublishedProfileWithoutPublicationTimestamp() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO personal_profiles (
                    id, full_name, slug, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                "Published Personal",
                "published-personal",
                "PUBLISHED",
                now,
                now))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicatedCityWideServiceArea() {
        UUID profileId = UUID.randomUUID();
        insertDraftProfile(profileId, "area-duplicada");
        insertServiceArea(profileId, "Umuarama", "PR", null);

        assertThatThrownBy(() ->
                insertServiceArea(profileId, "Umuarama", "PR", null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertDraftProfile(UUID id, String slug) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        jdbcTemplate.update("""
                INSERT INTO personal_profiles (
                    id, full_name, slug, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                id,
                "Test Personal",
                slug,
                "DRAFT",
                now,
                now);
    }

    private void insertServiceArea(
            UUID personalId,
            String city,
            String stateCode,
            String neighborhood) {
        jdbcTemplate.update("""
                INSERT INTO personal_service_areas (
                    id, personal_id, city, state_code, neighborhood, created_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(),
                personalId,
                city,
                stateCode,
                neighborhood,
                OffsetDateTime.now(ZoneOffset.UTC));
    }
}
