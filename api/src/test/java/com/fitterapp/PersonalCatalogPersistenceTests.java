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

import com.fitterapp.personal.entity.Modality;
import com.fitterapp.personal.entity.PersonalModality;
import com.fitterapp.personal.entity.PersonalModalityId;
import com.fitterapp.personal.entity.PersonalProfile;
import com.fitterapp.personal.entity.PersonalProfileStatus;
import com.fitterapp.personal.entity.PersonalServiceArea;
import com.fitterapp.personal.entity.PersonalServiceMode;
import com.fitterapp.personal.entity.PersonalServiceModeId;
import com.fitterapp.personal.entity.PriceUnit;
import com.fitterapp.personal.entity.ServiceMode;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersonalCatalogPersistenceTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void loadsPublishedProfileWithOptionalUserAndProfessionalData() {
        UUID userId = insertUser();
        UUID personalId = UUID.randomUUID();
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO personal_profiles (
                    id, user_id, full_name, slug, biography, whatsapp,
                    profile_image_key, experience_started_year, certifications,
                    gyms_description, starting_price_cents, price_unit, status,
                    published_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                personalId,
                userId,
                "Bruno Personal",
                "bruno-personal",
                "Treinamento individualizado.",
                "+5544999999999",
                "personals/" + personalId + "/" + UUID.randomUUID() + ".webp",
                (short) 2018,
                "CREF ativo",
                "Academia Fitter",
                15000,
                PriceUnit.PER_SESSION.name(),
                PersonalProfileStatus.PUBLISHED.name(),
                now,
                now.minusDays(1),
                now);

        entityManager.clear();
        PersonalProfile profile = entityManager.find(PersonalProfile.class, personalId);

        assertThat(profile).isNotNull();
        assertThat(profile.getUser().getId()).isEqualTo(userId);
        assertThat(profile.getFullName()).isEqualTo("Bruno Personal");
        assertThat(profile.getBiography()).isEqualTo("Treinamento individualizado.");
        assertThat(profile.getProfileImageKey()).endsWith(".webp");
        assertThat(profile.getExperienceStartedYear()).isEqualTo((short) 2018);
        assertThat(profile.getStartingPriceCents()).isEqualTo(15000);
        assertThat(profile.getPriceUnit()).isEqualTo(PriceUnit.PER_SESSION);
        assertThat(profile.getStatus()).isEqualTo(PersonalProfileStatus.PUBLISHED);
        assertThat(profile.getPublishedAt()).isEqualTo(now);
    }

    @Test
    void loadsModalityServiceModeAndAreaRelationships() {
        UUID personalId = insertDraftProfile();
        UUID areaId = UUID.randomUUID();
        OffsetDateTime createdAt = now();

        jdbcTemplate.update("""
                INSERT INTO personal_modalities (personal_id, modality_id)
                VALUES (?, ?)
                """, personalId, (short) 1);
        jdbcTemplate.update("""
                INSERT INTO personal_service_modes (personal_id, service_mode)
                VALUES (?, ?)
                """, personalId, ServiceMode.IN_PERSON.name());
        jdbcTemplate.update("""
                INSERT INTO personal_service_areas (
                    id, personal_id, city, state_code, neighborhood,
                    description, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                areaId,
                personalId,
                "Umuarama",
                "PR",
                "Zona I",
                "Atendimento na região central",
                createdAt);

        entityManager.clear();
        Modality modality = entityManager.find(Modality.class, (short) 1);
        PersonalModality personalModality = entityManager.find(
                PersonalModality.class,
                new PersonalModalityId(personalId, (short) 1));
        PersonalServiceMode serviceMode = entityManager.find(
                PersonalServiceMode.class,
                new PersonalServiceModeId(personalId, ServiceMode.IN_PERSON));
        PersonalServiceArea area = entityManager.find(PersonalServiceArea.class, areaId);

        assertThat(modality.getSlug()).isEqualTo("musculacao");
        assertThat(modality.isActive()).isTrue();
        assertThat(personalModality.getPersonal().getId()).isEqualTo(personalId);
        assertThat(personalModality.getModality().getName()).isEqualTo("Musculação");
        assertThat(serviceMode.getPersonal().getId()).isEqualTo(personalId);
        assertThat(serviceMode.getServiceMode()).isEqualTo(ServiceMode.IN_PERSON);
        assertThat(area.getPersonal().getId()).isEqualTo(personalId);
        assertThat(area.getCity()).isEqualTo("Umuarama");
        assertThat(area.getStateCode()).isEqualTo("PR");
        assertThat(area.getNeighborhood()).isEqualTo("Zona I");
        assertThat(area.getCreatedAt()).isEqualTo(createdAt);
    }

    private UUID insertDraftProfile() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO personal_profiles (
                    id, full_name, slug, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                """,
                id,
                "Persistence Personal",
                "persistence-" + id,
                PersonalProfileStatus.DRAFT.name(),
                now,
                now);

        return id;
    }

    private UUID insertUser() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = now();

        jdbcTemplate.update("""
                INSERT INTO users (
                    id, full_name, email, phone_number, password_hash,
                    status, email_verified_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                "Linked Personal",
                "linked-" + id + "@fitterapp.com",
                "+5544999999998",
                "test-password-hash",
                "ACTIVE",
                now,
                now,
                now);

        return id;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}
