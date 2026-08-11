package com.fitterapp.personal.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.analytics.entity.audit.AdminAuditLog;
import com.fitterapp.analytics.repository.AdminAuditLogRepository;
import com.fitterapp.auth.service.register.RegisterResult;
import com.fitterapp.auth.service.register.RegisterService;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.service.create.CreateProfileResult;
import com.fitterapp.personal.service.create.CreateProfileService;
import com.fitterapp.personal.service.cref.UpsertCrefCommand;
import com.fitterapp.personal.service.cref.UpsertCrefService;
import com.fitterapp.personal.service.modality.UpdateProfileModalitiesService;
import com.fitterapp.personal.service.service.UpdateProfileServiceAreasService;
import com.fitterapp.personal.service.service.UpdateProfileServiceModesService;
import com.fitterapp.personal.service.update.UpdateProfileDraftService;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdminPersonalManagementServiceTests {
  private static final Instant NOW = Instant.parse("2026-08-11T17:00:00Z");

  @Mock private RegisterService register;
  @Mock private CreateProfileService createProfile;
  @Mock private UpdateProfileDraftService updateDraft;
  @Mock private UpdateProfileModalitiesService updateModalities;
  @Mock private UpdateProfileServiceModesService updateModes;
  @Mock private UpdateProfileServiceAreasService updateAreas;
  @Mock private UpsertCrefService upsertCref;
  @Mock private ProfileRepository profiles;
  @Mock private UserRepository users;
  @Mock private AdminAuditLogRepository auditLogs;

  @Test
  void createsAccountAndDraftWithoutInventingCrefAndRecordsAdminOrigin() {
    UUID adminId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    UUID revisionId = UUID.randomUUID();
    User admin = user(adminId, "admin");
    when(users.findById(adminId)).thenReturn(Optional.of(admin));
    when(register.register(any())).thenReturn(new RegisterResult(userId));
    when(createProfile.create(any())).thenReturn(new CreateProfileResult(profileId, revisionId));

    var result =
        service()
            .create(
                new AdminCreatePersonalCommand(
                    adminId,
                    "New Personal",
                    "new@example.com",
                    "+5544999999999",
                    "temporary-password",
                    input(null, null),
                    "Cadastro solicitado pela operação"));

    assertThat(result).isEqualTo(new AdminPersonalResult(userId, profileId, revisionId));
    verify(upsertCref, never()).upsert(any());
    ArgumentCaptor<AdminAuditLog> audit = ArgumentCaptor.forClass(AdminAuditLog.class);
    verify(auditLogs).save(audit.capture());
    assertThat(audit.getValue().getAction()).isEqualTo("PERSONAL_PROFILE_ADMIN_CREATED");
    assertThat(audit.getValue().getNewState().get("origin").asText()).isEqualTo("ADMIN");
    assertThat(audit.getValue().getActor()).isSameAs(admin);
  }

  @Test
  void updatesEditableRevisionWithOptionalCrefAndRecordsAdminOrigin() {
    UUID adminId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    UUID revisionId = UUID.randomUUID();
    User admin = user(adminId, "admin");
    User owner = user(ownerId, "owner");
    Profile profile = Profile.draft("Owner", "owner", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(profile, "id", profileId);
    profile.linkUser(owner, NOW.atOffset(ZoneOffset.UTC));
    ProfileRevision revision =
        ProfileRevision.draft(profile, 1, owner, true, NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(revision, "id", revisionId);
    profile.setCurrentRevision(revision, NOW.atOffset(ZoneOffset.UTC));
    when(users.findById(adminId)).thenReturn(Optional.of(admin));
    when(profiles.findById(profileId)).thenReturn(Optional.of(profile));

    service()
        .update(
            new AdminUpdatePersonalCommand(
                adminId,
                profileId,
                input("123456-G/PR", "private/crefs/profile/document.webp"),
                "Dados enviados pelo suporte"));

    verify(upsertCref)
        .upsert(
            new UpsertCrefCommand(
                ownerId, profileId, "123456-G/PR", "private/crefs/profile/document.webp"));
    ArgumentCaptor<AdminAuditLog> audit = ArgumentCaptor.forClass(AdminAuditLog.class);
    verify(auditLogs).save(audit.capture());
    assertThat(audit.getValue().getAction()).isEqualTo("PERSONAL_PROFILE_ADMIN_UPDATED");
    assertThat(audit.getValue().getNewState().get("origin").asText()).isEqualTo("ADMIN");
  }

  private AdminPersonalManagementService service() {
    return new AdminPersonalManagementService(
        register,
        createProfile,
        updateDraft,
        updateModalities,
        updateModes,
        updateAreas,
        upsertCref,
        profiles,
        users,
        auditLogs,
        Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private AdminPersonalInput input(String crefCode, String documentKey) {
    return new AdminPersonalInput(
        "Professional Name",
        "Biography",
        "+5544999999999",
        (short) 2020,
        null,
        null,
        null,
        null,
        List.of(),
        List.of(),
        List.of(),
        crefCode,
        documentKey);
  }

  private User user(UUID id, String name) {
    User user =
        User.pendingRegistration(
            name,
            name + "@example.com",
            "+5544999999999",
            "hash",
            NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }
}
