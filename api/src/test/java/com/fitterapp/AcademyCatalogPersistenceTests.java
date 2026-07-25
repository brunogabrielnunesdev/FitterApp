package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.fitterapp.academy.entity.AcademyCnpj;
import com.fitterapp.academy.entity.AcademyCnpjStatus;
import com.fitterapp.academy.entity.AcademyMember;
import com.fitterapp.academy.entity.AcademyMemberId;
import com.fitterapp.academy.entity.AcademyMemberRole;
import com.fitterapp.academy.entity.AcademyMemberStatus;
import com.fitterapp.academy.entity.AcademyPersonalPartnership;
import com.fitterapp.academy.entity.AcademyProfile;
import com.fitterapp.academy.entity.AcademyProfileRevision;
import com.fitterapp.academy.entity.AcademyProfileRevisionStatus;
import com.fitterapp.academy.entity.AcademyProfileStatus;
import com.fitterapp.academy.entity.AcademyRevisionModality;
import com.fitterapp.academy.entity.AcademyRevisionModalityId;
import com.fitterapp.academy.entity.PartnershipInitiator;
import com.fitterapp.academy.entity.PartnershipStatus;
import com.fitterapp.personal.entity.Modality;
import com.fitterapp.personal.entity.PersonalProfile;
import com.fitterapp.user.entity.User;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AcademyCatalogPersistenceTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsReviewedAndPublishedAcademyRevision() {
        OffsetDateTime createdAt = now();
        User applicant = activeUser(
                "Academy Applicant",
                "academy-applicant@fitterapp.com",
                "+5544999999920",
                createdAt);
        User reviewer = activeUser(
                "Platform Owner",
                "academy-reviewer@fitterapp.com",
                "+5544999999921",
                createdAt);

        AcademyProfile academy = AcademyProfile.draft(
                "studio_performance",
                createdAt);
        entityManager.persist(academy);
        entityManager.flush();

        AcademyCnpj cnpj = AcademyCnpj.pendingReview(
                academy,
                "12345678000190",
                createdAt);
        entityManager.persist(cnpj);

        AcademyProfileRevision revision = AcademyProfileRevision.draft(
                academy,
                1,
                applicant,
                true,
                createdAt);
        revision.updatePublicData(
                "Studio Performance",
                "Treinamento próximo e individualizado.",
                "+5544999999920",
                "@studio.performance",
                "academies/" + academy.getId() + "/"
                        + java.util.UUID.randomUUID() + ".webp",
                createdAt);
        revision.updateAddress(
                "87501000",
                "Avenida Paraná",
                "1200",
                "Sala 2",
                "Zona I",
                "Umuarama",
                "PR",
                createdAt);
        revision.assignCnpj(cnpj, createdAt);
        entityManager.persist(revision);
        entityManager.flush();

        academy.setCurrentRevision(revision, createdAt);

        OffsetDateTime submittedAt = createdAt.plusMinutes(5);
        OffsetDateTime approvedAt = createdAt.plusMinutes(10);
        OffsetDateTime publishedAt = createdAt.plusMinutes(15);
        revision.submit(submittedAt);
        academy.submitForReview(submittedAt);
        cnpj.verify(reviewer, approvedAt);
        revision.approve(reviewer, approvedAt);
        academy.approve(approvedAt);
        academy.publish(revision, publishedAt);

        entityManager.flush();
        entityManager.clear();

        AcademyProfile savedAcademy = entityManager.find(
                AcademyProfile.class,
                academy.getId());
        AcademyProfileRevision savedRevision = entityManager.find(
                AcademyProfileRevision.class,
                revision.getId());
        AcademyCnpj savedCnpj = entityManager.find(
                AcademyCnpj.class,
                cnpj.getId());

        assertThat(savedAcademy.getStatus())
                .isEqualTo(AcademyProfileStatus.PUBLISHED);
        assertThat(savedAcademy.getCurrentRevision().getId())
                .isEqualTo(revision.getId());
        assertThat(savedAcademy.getPublishedRevision().getId())
                .isEqualTo(revision.getId());
        assertThat(savedRevision.getStatus())
                .isEqualTo(AcademyProfileRevisionStatus.APPROVED);
        assertThat(savedRevision.getName()).isEqualTo("Studio Performance");
        assertThat(savedRevision.getCity()).isEqualTo("Umuarama");
        assertThat(savedRevision.getCnpj().getId()).isEqualTo(cnpj.getId());
        assertThat(savedRevision.getReviewedBy().getId())
                .isEqualTo(reviewer.getId());
        assertThat(savedCnpj.getStatus()).isEqualTo(AcademyCnpjStatus.VERIFIED);
    }

    @Test
    void persistsAcademyOwnerAndVersionedModality() {
        OffsetDateTime createdAt = now();
        User owner = activeUser(
                "Academy Owner",
                "member-owner@fitterapp.com",
                "+5544999999922",
                createdAt);
        AcademyProfile academy = AcademyProfile.draft(
                "movimento_academia",
                createdAt);
        entityManager.persist(academy);
        entityManager.flush();

        AcademyMember member = AcademyMember.active(
                academy,
                owner,
                AcademyMemberRole.OWNER,
                createdAt);
        AcademyProfileRevision revision = AcademyProfileRevision.draft(
                academy,
                1,
                owner,
                true,
                createdAt);
        entityManager.persist(member);
        entityManager.persist(revision);
        entityManager.flush();

        Modality modality = entityManager.find(Modality.class, (short) 1);
        AcademyRevisionModality revisionModality =
                AcademyRevisionModality.link(revision, modality);
        entityManager.persist(revisionModality);
        entityManager.flush();
        entityManager.clear();

        AcademyMember savedMember = entityManager.find(
                AcademyMember.class,
                new AcademyMemberId(academy.getId(), owner.getId()));
        AcademyRevisionModality savedModality = entityManager.find(
                AcademyRevisionModality.class,
                new AcademyRevisionModalityId(revision.getId(), (short) 1));

        assertThat(savedMember.getRole()).isEqualTo(AcademyMemberRole.OWNER);
        assertThat(savedMember.getStatus()).isEqualTo(AcademyMemberStatus.ACTIVE);
        assertThat(savedMember.getUser().getId()).isEqualTo(owner.getId());
        assertThat(savedModality.getRevision().getId()).isEqualTo(revision.getId());
        assertThat(savedModality.getModality().getSlug()).isEqualTo("musculacao");
    }

    @Test
    void persistsBilateralPartnershipLifecycle() {
        OffsetDateTime createdAt = now();
        User academyOwner = activeUser(
                "Academy Partnership Owner",
                "partnership-academy@fitterapp.com",
                "+5544999999923",
                createdAt);
        User personalUser = activeUser(
                "Partnership Personal",
                "partnership-personal@fitterapp.com",
                "+5544999999924",
                createdAt);

        AcademyProfile academy = AcademyProfile.draft(
                "parceria_fitness",
                createdAt);
        PersonalProfile personal = PersonalProfile.draft(
                "Partnership Personal",
                "partnership-personal",
                createdAt);
        personal.linkUser(personalUser, createdAt);
        entityManager.persist(academy);
        entityManager.persist(personal);
        entityManager.flush();

        AcademyPersonalPartnership partnership =
                AcademyPersonalPartnership.request(
                        academy,
                        personal,
                        PartnershipInitiator.ACADEMY,
                        academyOwner,
                        createdAt);
        entityManager.persist(partnership);
        entityManager.flush();

        OffsetDateTime acceptedAt = createdAt.plusMinutes(5);
        partnership.accept(personalUser, acceptedAt);
        entityManager.flush();
        entityManager.clear();

        AcademyPersonalPartnership saved = entityManager.find(
                AcademyPersonalPartnership.class,
                partnership.getId());

        assertThat(saved.getStatus()).isEqualTo(PartnershipStatus.ACCEPTED);
        assertThat(saved.getInitiatedBy())
                .isEqualTo(PartnershipInitiator.ACADEMY);
        assertThat(saved.getRequestedBy().getId()).isEqualTo(academyOwner.getId());
        assertThat(saved.getRespondedBy().getId()).isEqualTo(personalUser.getId());
        assertThat(saved.getAcademy().getId()).isEqualTo(academy.getId());
        assertThat(saved.getPersonal().getId()).isEqualTo(personal.getId());
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
