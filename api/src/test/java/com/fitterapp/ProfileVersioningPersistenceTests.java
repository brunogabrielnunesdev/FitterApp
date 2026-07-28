package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.personal.entity.cref.Cref;
import com.fitterapp.personal.entity.cref.CrefStatus;
import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.entity.profile.RevisionModalityId;
import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import com.fitterapp.personal.entity.profile.RevisionServiceModeId;
import com.fitterapp.personal.entity.service.PriceUnit;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileVersioningPersistenceTests {

  @Autowired private EntityManager entityManager;

  @Test
  void persistsCrefReviewAndPublishedProfileRevision() {
    OffsetDateTime createdAt = now();
    User applicant =
        activeUser(
            "Applicant Personal",
            "applicant-versioning@fitterapp.com",
            "+5544999999910",
            createdAt);
    User owner =
        activeUser("Platform Owner", "owner-versioning@fitterapp.com", "+5544999999911", createdAt);

    Profile profile = Profile.draft("Applicant Personal", "applicant-personal", createdAt);
    profile.linkUser(applicant, createdAt);
    entityManager.persist(profile);
    entityManager.flush();

    Cref cref =
        Cref.pendingReview(
            profile,
            "012345-G/PR",
            "private/crefs/" + profile.getId() + "/" + java.util.UUID.randomUUID() + ".webp",
            createdAt);
    entityManager.persist(cref);

    ProfileRevision revision = ProfileRevision.draft(profile, 1, applicant, true, createdAt);
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

    Profile savedProfile = entityManager.find(Profile.class, profile.getId());
    ProfileRevision savedRevision = entityManager.find(ProfileRevision.class, revision.getId());
    Cref savedCref = entityManager.find(Cref.class, cref.getId());

    assertThat(savedProfile.getStatus()).isEqualTo(ProfileStatus.PUBLISHED);
    assertThat(savedProfile.getCurrentRevision().getId()).isEqualTo(revision.getId());
    assertThat(savedProfile.getPublishedRevision().getId()).isEqualTo(revision.getId());
    assertThat(savedRevision.getStatus()).isEqualTo(ProfileRevisionStatus.APPROVED);
    assertThat(savedRevision.getCreatedBy().getId()).isEqualTo(applicant.getId());
    assertThat(savedRevision.getReviewedBy().getId()).isEqualTo(owner.getId());
    assertThat(savedRevision.getCref().getId()).isEqualTo(cref.getId());
    assertThat(savedRevision.getPriceUnit()).isEqualTo(PriceUnit.PER_SESSION);
    assertThat(savedCref.getStatus()).isEqualTo(CrefStatus.VERIFIED);
    assertThat(savedCref.getVerifiedBy().getId()).isEqualTo(owner.getId());
  }

  @Test
  void persistsVersionedModalitiesServiceModesAndAreas() {
    OffsetDateTime createdAt = now();
    User applicant =
        activeUser(
            "Relationship Personal",
            "relationships-versioning@fitterapp.com",
            "+5544999999912",
            createdAt);
    Profile profile = Profile.draft("Relationship Personal", "relationship-personal", createdAt);
    profile.linkUser(applicant, createdAt);
    entityManager.persist(profile);
    entityManager.flush();

    ProfileRevision revision = ProfileRevision.draft(profile, 1, applicant, true, createdAt);
    entityManager.persist(revision);
    entityManager.flush();

    Modality modality = entityManager.find(Modality.class, (short) 1);
    RevisionModality revisionModality = RevisionModality.link(revision, modality);
    RevisionServiceMode serviceMode = RevisionServiceMode.of(revision, ServiceMode.IN_PERSON);
    RevisionServiceArea serviceArea =
        RevisionServiceArea.create(
            revision, "Umuarama", "PR", "Zona I", "Atendimento na região central", createdAt);

    entityManager.persist(revisionModality);
    entityManager.persist(serviceMode);
    entityManager.persist(serviceArea);
    entityManager.flush();
    entityManager.clear();

    RevisionModality savedModality =
        entityManager.find(
            RevisionModality.class, new RevisionModalityId(revision.getId(), (short) 1));
    RevisionServiceMode savedMode =
        entityManager.find(
            RevisionServiceMode.class,
            new RevisionServiceModeId(revision.getId(), ServiceMode.IN_PERSON));
    RevisionServiceArea savedArea =
        entityManager.find(RevisionServiceArea.class, serviceArea.getId());

    assertThat(savedModality.getRevision().getId()).isEqualTo(revision.getId());
    assertThat(savedModality.getModality().getSlug()).isEqualTo("musculacao");
    assertThat(savedMode.getRevision().getId()).isEqualTo(revision.getId());
    assertThat(savedMode.getServiceMode()).isEqualTo(ServiceMode.IN_PERSON);
    assertThat(savedArea.getRevision().getId()).isEqualTo(revision.getId());
    assertThat(savedArea.getCity()).isEqualTo("Umuarama");
    assertThat(savedArea.getStateCode()).isEqualTo("PR");
  }

  private User activeUser(String fullName, String email, String phone, OffsetDateTime createdAt) {
    User user = User.pendingRegistration(fullName, email, phone, "test-password-hash", createdAt);
    user.confirmEmail(createdAt);
    entityManager.persist(user);
    return user;
  }

  private OffsetDateTime now() {
    return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
  }
}
