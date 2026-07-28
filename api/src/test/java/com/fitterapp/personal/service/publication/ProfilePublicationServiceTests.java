package com.fitterapp.personal.service.publication;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.personal.exception.*;
import com.fitterapp.personal.repository.*;
import com.fitterapp.user.entity.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfilePublicationServiceTests {
  static final Instant NOW = Instant.parse("2026-07-27T22:00:00Z");
  @Mock ProfileRepository profiles;

  @Test
  void publishesApprovedRevision() {
    F f = f(true);
    when(profiles.findByIdAndUserId(f.profileId, f.userId)).thenReturn(Optional.of(f.profile));
    service().publish(new PublishProfileCommand(f.userId, f.profileId));
    assertThat(f.profile.getStatus()).isEqualTo(ProfileStatus.PUBLISHED);
    assertThat(f.profile.getPublishedRevision()).isSameAs(f.revision);
  }

  @Test
  void rejectsPublishingNonApprovedRevision() {
    F f = f(false);
    when(profiles.findByIdAndUserId(f.profileId, f.userId)).thenReturn(Optional.of(f.profile));
    assertThatThrownBy(() -> service().publish(new PublishProfileCommand(f.userId, f.profileId)))
        .isInstanceOf(ProfileNotApprovedException.class);
  }

  @Test
  void unpublishesPublishedProfile() {
    F f = f(true);
    f.profile.publish(f.revision, NOW.atOffset(ZoneOffset.UTC));
    when(profiles.findByIdAndUserId(f.profileId, f.userId)).thenReturn(Optional.of(f.profile));
    service().unpublish(new UnpublishProfileCommand(f.userId, f.profileId));
    assertThat(f.profile.getStatus()).isEqualTo(ProfileStatus.APPROVED);
    assertThat(f.profile.getPublishedRevision()).isNull();
  }

  private ProfilePublicationService service() {
    return new ProfilePublicationService(profiles, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private F f(boolean approved) {
    UUID uid = UUID.randomUUID(), pid = UUID.randomUUID(), rid = UUID.randomUUID();
    User u =
        User.pendingRegistration(
            "B", "b@x.com", "+5544999999999", "h", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(u, "id", uid);
    Profile p = Profile.draft("B", "b", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(p, "id", pid);
    p.linkUser(u, NOW.atOffset(ZoneOffset.UTC));
    ProfileRevision r = ProfileRevision.draft(p, 1, u, true, NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(r, "id", rid);
    if (approved) {
      r.submit(NOW.atOffset(ZoneOffset.UTC));
      r.approve(u, NOW.atOffset(ZoneOffset.UTC));
      p.approve(NOW.atOffset(ZoneOffset.UTC));
    }
    p.setCurrentRevision(r, NOW.atOffset(ZoneOffset.UTC));
    return new F(uid, pid, p, r);
  }

  record F(UUID userId, UUID profileId, Profile profile, ProfileRevision revision) {}
}
