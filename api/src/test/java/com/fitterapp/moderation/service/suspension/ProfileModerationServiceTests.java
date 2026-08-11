package com.fitterapp.moderation.service.suspension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.analytics.entity.audit.AdminAuditLog;
import com.fitterapp.analytics.repository.AdminAuditLogRepository;
import com.fitterapp.moderation.entity.suspension.ProfileSuspension;
import com.fitterapp.moderation.entity.suspension.SuspensionStatus;
import com.fitterapp.moderation.exception.ModerationReasonRequiredException;
import com.fitterapp.moderation.exception.ProfileModerationStateException;
import com.fitterapp.moderation.repository.ProfileSuspensionRepository;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfileModerationServiceTests {
  private static final Instant NOW = Instant.parse("2026-08-11T15:00:00Z");

  @Mock private ProfileRepository profiles;
  @Mock private ProfileSuspensionRepository suspensions;
  @Mock private UserRepository users;
  @Mock private AdminAuditLogRepository auditLogs;

  @Test
  void suspendsPublishedProfileAndAuditsTheAction() {
    Fixture fixture = fixture(true);
    when(profiles.findById(fixture.profileId())).thenReturn(Optional.of(fixture.profile()));
    when(users.findById(fixture.adminId())).thenReturn(Optional.of(fixture.admin()));

    var result =
        service()
            .suspend(
                new SuspendProfileCommand(
                    fixture.adminId(), fixture.profileId(), "  Informação irregular  "));

    assertThat(result.profileStatus()).isEqualTo(ProfileStatus.SUSPENDED);
    assertThat(fixture.profile().getPublishedRevision()).isSameAs(fixture.revision());
    ArgumentCaptor<ProfileSuspension> suspensionCaptor =
        ArgumentCaptor.forClass(ProfileSuspension.class);
    verify(suspensions).save(suspensionCaptor.capture());
    assertThat(suspensionCaptor.getValue().getPreviousStatus()).isEqualTo("PUBLISHED");
    assertThat(suspensionCaptor.getValue().getReason()).isEqualTo("Informação irregular");
    assertThat(suspensionCaptor.getValue().getSuspendedBy()).isSameAs(fixture.admin());
    assertThat(suspensionCaptor.getValue().getSuspendedAt())
        .isEqualTo(NOW.atOffset(ZoneOffset.UTC));

    ArgumentCaptor<AdminAuditLog> auditCaptor = ArgumentCaptor.forClass(AdminAuditLog.class);
    verify(auditLogs).save(auditCaptor.capture());
    assertThat(auditCaptor.getValue().getAction()).isEqualTo("PERSONAL_PROFILE_SUSPENDED");
    assertThat(auditCaptor.getValue().getPreviousState().get("status").asText())
        .isEqualTo("PUBLISHED");
    assertThat(auditCaptor.getValue().getNewState().get("status").asText()).isEqualTo("SUSPENDED");
  }

  @Test
  void reactivationRestoresPublishedCatalogStateAndAuditsTheAction() {
    Fixture fixture = fixture(true);
    OffsetDateTime now = NOW.atOffset(ZoneOffset.UTC);
    fixture.profile().suspend(now.minusDays(1));
    var suspension =
        ProfileSuspension.suspendPersonal(
            fixture.profile(),
            null,
            fixture.admin(),
            "Suspensão preventiva",
            "PUBLISHED",
            now.minusDays(1),
            now.minusHours(12));
    UUID suspensionId = UUID.randomUUID();
    ReflectionTestUtils.setField(suspension, "id", suspensionId);
    when(profiles.findById(fixture.profileId())).thenReturn(Optional.of(fixture.profile()));
    when(suspensions.findByPersonalProfileIdAndStatus(fixture.profileId(), SuspensionStatus.ACTIVE))
        .thenReturn(Optional.of(suspension));
    when(users.findById(fixture.adminId())).thenReturn(Optional.of(fixture.admin()));

    var result =
        service()
            .reactivate(
                new ReactivateProfileCommand(
                    fixture.adminId(), fixture.profileId(), "  Correção validada  "));

    assertThat(result.suspensionId()).isEqualTo(suspensionId);
    assertThat(fixture.profile().getStatus()).isEqualTo(ProfileStatus.PUBLISHED);
    assertThat(fixture.profile().getPublishedRevision()).isSameAs(fixture.revision());
    assertThat(suspension.getStatus()).isEqualTo(SuspensionStatus.LIFTED);
    assertThat(suspension.getLiftReason()).isEqualTo("Correção validada");
    assertThat(suspension.getLiftedBy()).isSameAs(fixture.admin());
    verify(auditLogs).save(any(AdminAuditLog.class));
  }

  @Test
  void reactivationRestoresApprovedUnpublishedState() {
    Fixture fixture = fixture(false);
    OffsetDateTime now = NOW.atOffset(ZoneOffset.UTC);
    fixture.profile().suspend(now.minusDays(1));
    var suspension =
        ProfileSuspension.suspendPersonal(
            fixture.profile(),
            null,
            fixture.admin(),
            "Revisão necessária",
            "APPROVED",
            now.minusDays(1),
            now.minusHours(12));
    when(profiles.findById(fixture.profileId())).thenReturn(Optional.of(fixture.profile()));
    when(suspensions.findByPersonalProfileIdAndStatus(fixture.profileId(), SuspensionStatus.ACTIVE))
        .thenReturn(Optional.of(suspension));
    when(users.findById(fixture.adminId())).thenReturn(Optional.of(fixture.admin()));

    service()
        .reactivate(
            new ReactivateProfileCommand(
                fixture.adminId(), fixture.profileId(), "Perfil regularizado"));

    assertThat(fixture.profile().getStatus()).isEqualTo(ProfileStatus.APPROVED);
    assertThat(fixture.profile().getPublishedRevision()).isNull();
  }

  @Test
  void rejectsSuspensionBeforeApproval() {
    Fixture fixture = draftFixture();
    when(profiles.findById(fixture.profileId())).thenReturn(Optional.of(fixture.profile()));

    assertThatThrownBy(
            () ->
                service()
                    .suspend(
                        new SuspendProfileCommand(
                            fixture.adminId(), fixture.profileId(), "Motivo")))
        .isInstanceOf(ProfileModerationStateException.class);
    verify(suspensions, never()).save(any());
  }

  @Test
  void requiresReasonBeforeLoadingProfile() {
    assertThatThrownBy(
            () ->
                service()
                    .suspend(new SuspendProfileCommand(UUID.randomUUID(), UUID.randomUUID(), "  ")))
        .isInstanceOf(ModerationReasonRequiredException.class);
    verify(profiles, never()).findById(any());
  }

  private ProfileModerationService service() {
    return new ProfileModerationService(
        profiles, suspensions, users, auditLogs, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private Fixture fixture(boolean published) {
    Fixture fixture = draftFixture();
    OffsetDateTime now = NOW.atOffset(ZoneOffset.UTC);
    fixture.revision().submit(now.minusDays(2));
    fixture.revision().approve(fixture.admin(), now.minusDays(1));
    fixture.profile().approve(now.minusDays(1));
    if (published) fixture.profile().publish(fixture.revision(), now.minusHours(12));
    return fixture;
  }

  private Fixture draftFixture() {
    OffsetDateTime now = NOW.atOffset(ZoneOffset.UTC);
    UUID ownerId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    User owner = user(ownerId, "owner");
    User admin = user(adminId, "admin");
    Profile profile = Profile.draft("Personal", "personal", now.minusDays(3));
    ReflectionTestUtils.setField(profile, "id", profileId);
    profile.linkUser(owner, now.minusDays(3));
    ProfileRevision revision = ProfileRevision.draft(profile, 1, owner, true, now.minusDays(3));
    ReflectionTestUtils.setField(revision, "id", UUID.randomUUID());
    profile.setCurrentRevision(revision, now.minusDays(3));
    return new Fixture(adminId, profileId, profile, revision, admin);
  }

  private User user(UUID id, String name) {
    User user =
        User.pendingRegistration(
            name, name + id + "@test.com", "+5544999999999", "hash", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  private record Fixture(
      UUID adminId, UUID profileId, Profile profile, ProfileRevision revision, User admin) {}
}
