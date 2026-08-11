package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.analytics.repository.AdminAuditLogRepository;
import com.fitterapp.moderation.entity.suspension.SuspensionStatus;
import com.fitterapp.moderation.repository.ProfileSuspensionRepository;
import com.fitterapp.moderation.service.suspension.ProfileModerationService;
import com.fitterapp.moderation.service.suspension.ReactivateProfileCommand;
import com.fitterapp.moderation.service.suspension.SuspendProfileCommand;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
  TestcontainersConfiguration.class,
  ProfileModerationService.class,
  ProfileModerationPersistenceTests.ClockConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileModerationPersistenceTests {
  private static final Instant NOW = Instant.parse("2026-08-11T15:00:00Z");

  @Autowired private EntityManager entityManager;
  @Autowired private ProfileRepository profiles;
  @Autowired private ProfileSuspensionRepository suspensions;
  @Autowired private AdminAuditLogRepository auditLogs;
  @Autowired private ProfileModerationService moderation;

  @Test
  void suspensionRemovesProfileFromCatalogAndReactivationRestoresIt() {
    OffsetDateTime now = NOW.atOffset(ZoneOffset.UTC);
    User owner =
        activeUser("Catalog Personal", "moderated-catalog@fitterapp.test", now.minusDays(3));
    User admin = activeUser("Catalog Admin", "moderation-admin@fitterapp.test", now.minusDays(3));
    Profile profile = Profile.draft("Catalog Personal", "moderated-catalog", now.minusDays(3));
    profile.linkUser(owner, now.minusDays(3));
    entityManager.persist(profile);
    entityManager.flush();

    ProfileRevision revision = ProfileRevision.draft(profile, 1, owner, true, now.minusDays(3));
    revision.updateProfessionalData(
        "Catalog Personal",
        "Perfil publicado para testar moderação.",
        "+5544999999999",
        (short) 2020,
        null,
        null,
        now.minusDays(3));
    entityManager.persist(revision);
    entityManager.flush();
    profile.setCurrentRevision(revision, now.minusDays(3));
    revision.submit(now.minusDays(2));
    revision.approve(admin, now.minusDays(1));
    profile.approve(now.minusDays(1));
    profile.publish(revision, now.minusHours(12));
    entityManager.flush();

    assertThat(profiles.findPublishedBySlug("moderated-catalog")).isPresent();

    var suspended =
        moderation.suspend(
            new SuspendProfileCommand(admin.getId(), profile.getId(), "Informação em análise"));
    entityManager.flush();
    entityManager.clear();

    assertThat(suspended.suspensionId()).isNotNull();
    assertThat(profiles.findPublishedBySlug("moderated-catalog")).isEmpty();
    assertThat(profiles.findById(profile.getId()).orElseThrow().getStatus())
        .isEqualTo(ProfileStatus.SUSPENDED);
    assertThat(
            suspensions
                .findByPersonalProfileIdAndStatus(profile.getId(), SuspensionStatus.ACTIVE)
                .orElseThrow()
                .getReason())
        .isEqualTo("Informação em análise");

    moderation.reactivate(
        new ReactivateProfileCommand(admin.getId(), profile.getId(), "Regularização confirmada"));
    entityManager.flush();
    entityManager.clear();

    assertThat(profiles.findPublishedBySlug("moderated-catalog")).isPresent();
    assertThat(profiles.findById(profile.getId()).orElseThrow().getStatus())
        .isEqualTo(ProfileStatus.PUBLISHED);
    assertThat(auditLogs.count()).isEqualTo(2);
  }

  private User activeUser(String name, String email, OffsetDateTime createdAt) {
    User user =
        User.pendingRegistration(name, email, "+5544999999999", "test-password-hash", createdAt);
    user.confirmEmail(createdAt);
    entityManager.persist(user);
    return user;
  }

  @TestConfiguration
  static class ClockConfig {
    @Bean
    Clock clock() {
      return Clock.fixed(NOW, ZoneOffset.UTC);
    }
  }
}
