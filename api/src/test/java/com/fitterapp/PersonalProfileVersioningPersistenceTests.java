package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.fitterapp.personal.entity.Modality;
import com.fitterapp.personal.entity.PersonalCref;
import com.fitterapp.personal.entity.PersonalCrefStatus;
import com.fitterapp.personal.entity.PersonalProfile;
import com.fitterapp.personal.entity.PersonalProfileRevision;
import com.fitterapp.personal.entity.PersonalProfileRevisionStatus;
import com.fitterapp.personal.entity.PersonalProfileStatus;
import com.fitterapp.personal.entity.PersonalRevisionModality;
import com.fitterapp.personal.entity.PersonalRevisionModalityId;
import com.fitterapp.personal.entity.PersonalRevisionServiceArea;
import com.fitterapp.personal.entity.PersonalRevisionServiceMode;
import com.fitterapp.personal.entity.PersonalRevisionServiceModeId;
import com.fitterapp.personal.entity.PriceUnit;
import com.fitterapp.personal.entity.ServiceMode;
import com.fitterapp.user.entity.User;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersonalProfileVersioningPersistenceTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsCrefReviewAndPublishedProfileRevision() {
        OffsetDateTime createdAt = now();
        User applicant = activeUser(
                "Applicant Personal",
                "applicant-versioning@fitterapp.com",
                "+5544999999910",
                createdAt);
        User owner = activeUser(
                "Platform Owner",
                "owner-versioning@fitterapp.com",
                "+5544999999911",
                createdAt);

        PersonalProfile profile = PersonalProfile.draft(
                "Applicant Personal",
                "applicant-personal",
                createdAt);
        profile.linkUser(applicant, createdAt);
        entityManager.persist(profile);
        entityManager.flush();

        PersonalCref cref = PersonalCref.pendingReview(
                profile,
                "012345-G/PR",
                "private/crefs/" + profile.getId() + "/"
                        + java.util.UUID.randomUUID() + ".webp",
                createdAt);
        entityManager.persist(cref);

        PersonalProfileRevision revision = PersonalProfileRevision.draft(
                profile,
                1,
                applicant,
                true,
                createdAt);
        revision.updateProfessionalData(
                "Applicant Personal",
                "Atendimento individualizado.",
                "+5544999999910",
                (short) 2019,
                "Especialização em treinamento funcional",
                null,
                createdAt);
        revision.updateStartingPrice(12000, PriceUnit.PER_SESSION, createdAt);
        revision.assignCref(cref, createdAt);
        entityManager.persist(revision);
        entityManager.flush();

        profile.setCurrentRevision(revision, createdAt);

        OffsetDateTime submittedAt = createdAt.plusMinutes(5);
        OffsetDateTime approvedAt = createdAt.plusMinutes(10);
        OffsetDateTime publishedAt = createdAt.plusMinutes(15);
        revision.submit(submittedAt);
        cref.verify(owner, approvedAt);
        revision.approve(owner, approvedAt);
        profile.approve(approvedAt);
        profile.publish(revision, publishedAt);

        entityManager.flush();
        entityManager.clear();

        PersonalProfile savedProfile = entityManager.find(
                PersonalProfile.class,
                profile.getId());
        PersonalProfileRevision savedRevision = entityManager.find(
                PersonalProfileRevision.class,
                revision.getId());
        PersonalCref savedCref = entityManager.find(PersonalCref.class, cref.getId());

        assertThat(savedProfile.getStatus()).isEqualTo(PersonalProfileStatus.PUBLISHED);
        assertThat(savedProfile.getCurrentRevision().getId()).isEqualTo(revision.getId());
        assertThat(savedProfile.getPublishedRevision().getId()).isEqualTo(revision.getId());
        assertThat(savedRevision.getStatus())
                .isEqualTo(PersonalProfileRevisionStatus.APPROVED);
        assertThat(savedRevision.getCreatedBy().getId()).isEqualTo(applicant.getId());
        assertThat(savedRevision.getReviewedBy().getId()).isEqualTo(owner.getId());
        assertThat(savedRevision.getCref().getId()).isEqualTo(cref.getId());
        assertThat(savedRevision.getPriceUnit()).isEqualTo(PriceUnit.PER_SESSION);
        assertThat(savedCref.getStatus()).isEqualTo(PersonalCrefStatus.VERIFIED);
        assertThat(savedCref.getVerifiedBy().getId()).isEqualTo(owner.getId());
    }

    @Test
    void persistsVersionedModalitiesServiceModesAndAreas() {
        OffsetDateTime createdAt = now();
        User applicant = activeUser(
                "Relationship Personal",
                "relationships-versioning@fitterapp.com",
                "+5544999999912",
                createdAt);
        PersonalProfile profile = PersonalProfile.draft(
                "Relationship Personal",
                "relationship-personal",
                createdAt);
        profile.linkUser(applicant, createdAt);
        entityManager.persist(profile);
        entityManager.flush();

        PersonalProfileRevision revision = PersonalProfileRevision.draft(
                profile,
                1,
                applicant,
                true,
                createdAt);
        entityManager.persist(revision);
        entityManager.flush();

        Modality modality = entityManager.find(Modality.class, (short) 1);
        PersonalRevisionModality revisionModality =
                PersonalRevisionModality.link(revision, modality);
        PersonalRevisionServiceMode serviceMode =
                PersonalRevisionServiceMode.of(revision, ServiceMode.IN_PERSON);
        PersonalRevisionServiceArea serviceArea = PersonalRevisionServiceArea.create(
                revision,
                "Umuarama",
                "PR",
                "Zona I",
                "Atendimento na região central",
                createdAt);

        entityManager.persist(revisionModality);
        entityManager.persist(serviceMode);
        entityManager.persist(serviceArea);
        entityManager.flush();
        entityManager.clear();

        PersonalRevisionModality savedModality = entityManager.find(
                PersonalRevisionModality.class,
                new PersonalRevisionModalityId(revision.getId(), (short) 1));
        PersonalRevisionServiceMode savedMode = entityManager.find(
                PersonalRevisionServiceMode.class,
                new PersonalRevisionServiceModeId(revision.getId(), ServiceMode.IN_PERSON));
        PersonalRevisionServiceArea savedArea = entityManager.find(
                PersonalRevisionServiceArea.class,
                serviceArea.getId());

        assertThat(savedModality.getRevision().getId()).isEqualTo(revision.getId());
        assertThat(savedModality.getModality().getSlug()).isEqualTo("musculacao");
        assertThat(savedMode.getRevision().getId()).isEqualTo(revision.getId());
        assertThat(savedMode.getServiceMode()).isEqualTo(ServiceMode.IN_PERSON);
        assertThat(savedArea.getRevision().getId()).isEqualTo(revision.getId());
        assertThat(savedArea.getCity()).isEqualTo("Umuarama");
        assertThat(savedArea.getStateCode()).isEqualTo("PR");
    }

    private User activeUser(
            String fullName,
            String email,
            String phone,
            OffsetDateTime createdAt) {
        User user = User.pendingRegistration(
                fullName,
                email,
                phone,
                "test-password-hash",
                createdAt);
        user.confirmEmail(createdAt);
        entityManager.persist(user);
        return user;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    }
}
