package com.fitterapp.personal.service.review;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fitterapp.moderation.entity.suspension.*;
import com.fitterapp.moderation.repository.ProfileSuspensionRepository;
import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.repository.UserRepository;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class AdminProfileLifecycleServiceTests {
  private final ProfileRepository profiles = mock(ProfileRepository.class);
  private final ProfileSuspensionRepository suspensions = mock(ProfileSuspensionRepository.class);
  private final UserRepository users = mock(UserRepository.class);
  private final Instant instant = Instant.parse("2026-07-27T21:00:00Z");
  private final UUID profileId = UUID.randomUUID(), adminId = UUID.randomUUID();
  private final User admin =
      User.pendingRegistration(
          "Admin", "admin@example.com", "123", "hash", instant.atOffset(ZoneOffset.UTC));

  private AdminProfileLifecycleService service(Instant now) {
    return new AdminProfileLifecycleService(
        profiles, suspensions, users, Clock.fixed(now, ZoneOffset.UTC));
  }

  @Test
  void suspendsAndRestoresPublishedProfile() {
    var profile = Profile.draft("Name", "name", instant.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(profile, "id", profileId);
    var revision = ProfileRevision.draft(profile, 1, admin, true, instant.atOffset(ZoneOffset.UTC));
    profile.publish(revision, instant.atOffset(ZoneOffset.UTC));
    when(profiles.findByIdForUpdate(profileId)).thenReturn(Optional.of(profile));
    when(users.findById(adminId)).thenReturn(Optional.of(admin));
    when(suspensions.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    service(instant).suspend(profileId, adminId, " motivo ");
    assertThat(profile.getStatus()).isEqualTo(ProfileStatus.SUSPENDED);
    var captured = org.mockito.ArgumentCaptor.forClass(ProfileSuspension.class);
    verify(suspensions).saveAndFlush(captured.capture());
    var suspension = captured.getValue();
    assertThat(suspension.getPreviousStatus()).isEqualTo("PUBLISHED");
    assertThat(suspension.getReason()).isEqualTo("motivo");
    when(suspensions.findFirstByPersonalProfileIdAndStatusOrderBySuspendedAtDesc(
            profileId, SuspensionStatus.ACTIVE))
        .thenReturn(Optional.of(suspension));
    service(instant.plusSeconds(2)).reactivate(profileId, adminId, " revisão ");
    assertThat(profile.getStatus()).isEqualTo(ProfileStatus.PUBLISHED);
    assertThat(profile.getPublishedRevision()).isSameAs(revision);
    assertThat(suspension.getStatus()).isEqualTo(SuspensionStatus.LIFTED);
    assertThat(suspension.getLiftReason()).isEqualTo("revisão");
  }

  @Test
  void refusesSuspensionOfDraft() {
    var profile = Profile.draft("Name", "name", instant.atOffset(ZoneOffset.UTC));
    when(profiles.findByIdForUpdate(profileId)).thenReturn(Optional.of(profile));
    assertThatThrownBy(() -> service(instant).suspend(profileId, adminId, "motivo"))
        .isInstanceOf(ResponseStatusException.class);
    verify(suspensions, never()).saveAndFlush(any());
  }
}
