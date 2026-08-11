package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.personal.entity.cref.Cref;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminProfileQueryPersistenceTests {

  @Autowired private EntityManager entityManager;
  @Autowired private ProfileRepository profiles;

  @Test
  void filtersAndLoadsEverythingNeededByAdministrativeQueries() {
    OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    User applicant =
        User.pendingRegistration(
            "Admin Query Personal",
            "admin-query@fitterapp.test",
            "+5544999999930",
            "test-password-hash",
            timestamp);
    applicant.confirmEmail(timestamp);
    entityManager.persist(applicant);

    Profile profile = Profile.draft("Admin Query Personal", "admin-query-personal", timestamp);
    profile.linkUser(applicant, timestamp);
    entityManager.persist(profile);
    entityManager.flush();

    Cref cref =
        Cref.pendingReview(
            profile,
            "987654-G/PR",
            "private/crefs/" + profile.getId() + "/" + java.util.UUID.randomUUID() + ".webp",
            timestamp);
    entityManager.persist(cref);

    ProfileRevision revision = ProfileRevision.draft(profile, 1, applicant, true, timestamp);
    revision.updateProfessionalData(
        "Admin Query Personal",
        "Perfil usado para validar consultas administrativas.",
        "+5544999999930",
        (short) 2020,
        "Certificação de teste",
        "Academia de teste",
        timestamp);
    revision.assignCref(cref, timestamp);
    entityManager.persist(revision);
    entityManager.flush();

    profile.setCurrentRevision(revision, timestamp);
    revision.submit(timestamp.plusMinutes(1));
    profile.submitForReview(timestamp.plusMinutes(1));
    entityManager.flush();
    entityManager.clear();

    var page =
        profiles.findAllForAdministration(ProfileStatus.PENDING_REVIEW, PageRequest.of(0, 10));

    assertThat(page.getTotalElements()).isEqualTo(1);
    assertThat(page.getContent())
        .singleElement()
        .satisfies(
            item -> {
              assertThat(item.getId()).isEqualTo(profile.getId());
              assertThat(Hibernate.isInitialized(item.getUser())).isTrue();
              assertThat(Hibernate.isInitialized(item.getCurrentRevision())).isTrue();
            });

    assertThat(profiles.findAllForAdministration(null, PageRequest.of(0, 10)).getTotalElements())
        .isEqualTo(1);

    var detail = profiles.findByIdForAdministration(profile.getId()).orElseThrow();
    assertThat(Hibernate.isInitialized(detail.getUser())).isTrue();
    assertThat(Hibernate.isInitialized(detail.getCurrentRevision())).isTrue();
    assertThat(Hibernate.isInitialized(detail.getCurrentRevision().getCref())).isTrue();
    assertThat(detail.getCurrentRevision().getCref().getRegistrationCode())
        .isEqualTo("987654-G/PR");
  }
}
