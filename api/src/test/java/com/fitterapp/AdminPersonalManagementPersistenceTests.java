package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.analytics.repository.AdminAuditLogRepository;
import com.fitterapp.auth.repository.EmailVerificationTokenRepository;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.service.admin.AdminCreatePersonalCommand;
import com.fitterapp.personal.service.admin.AdminPersonalInput;
import com.fitterapp.personal.service.admin.AdminPersonalManagementService;
import com.fitterapp.personal.service.admin.AdminUpdatePersonalCommand;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class AdminPersonalManagementPersistenceTests {
  @Autowired private EntityManager entityManager;
  @Autowired private AdminPersonalManagementService service;
  @Autowired private UserRepository users;
  @Autowired private ProfileRepository profiles;
  @Autowired private EmailVerificationTokenRepository verificationTokens;
  @Autowired private AdminAuditLogRepository auditLogs;

  @Test
  void createsAndEditsAdministrativePersonalWithAuditTrail() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    User admin =
        User.pendingRegistration(
            "Operations Admin",
            "operations.admin@example.com",
            "+5544999999901",
            "admin-password-hash",
            now);
    admin.confirmEmail(now);
    entityManager.persist(admin);
    entityManager.flush();

    var created =
        service.create(
            new AdminCreatePersonalCommand(
                admin.getId(),
                "New Personal",
                "admin-created.personal@example.com",
                "+5544999999902",
                "temporary-password",
                input(null, null),
                "Solicitação da operação"));
    entityManager.flush();

    var account = users.findById(created.userId()).orElseThrow();
    var profile = profiles.findById(created.profileId()).orElseThrow();
    assertThat(account.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
    assertThat(profile.getCurrentRevision().getCref()).isNull();
    assertThat(verificationTokens.count()).isEqualTo(1);

    String documentKey =
        "private/crefs/" + profile.getId() + "/" + UUID.randomUUID() + ".webp";
    service.update(
        new AdminUpdatePersonalCommand(
            admin.getId(),
            profile.getId(),
            input("123456-G/PR", documentKey),
            "Complemento cadastral"));
    entityManager.flush();
    entityManager.clear();

    var updated = profiles.findById(created.profileId()).orElseThrow();
    assertThat(updated.getCurrentRevision().getFullName()).isEqualTo("Professional Name");
    assertThat(updated.getCurrentRevision().getCref().getRegistrationCode())
        .isEqualTo("123456-G/PR");
    assertThat(auditLogs.findAll())
        .extracting(log -> log.getAction())
        .containsExactlyInAnyOrder(
            "PERSONAL_PROFILE_ADMIN_CREATED", "PERSONAL_PROFILE_ADMIN_UPDATED");
    assertThat(auditLogs.findAll())
        .allSatisfy(
            log -> {
              assertThat(log.getActor().getId()).isEqualTo(admin.getId());
              assertThat(log.getNewState().get("origin").asText()).isEqualTo("ADMIN");
            });
  }

  private AdminPersonalInput input(String crefCode, String documentKey) {
    return new AdminPersonalInput(
        "Professional Name",
        "Biography",
        "+5544999999902",
        (short) 2020,
        null,
        null,
        null,
        null,
        List.of(),
        List.of(ServiceMode.ONLINE),
        List.of(),
        crefCode,
        documentKey);
  }
}
