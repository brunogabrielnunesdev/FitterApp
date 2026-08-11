package com.fitterapp.personal.service.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.analytics.entity.event.FunnelEvent;
import com.fitterapp.analytics.entity.event.FunnelEventType;
import com.fitterapp.analytics.repository.FunnelEventRepository;
import com.fitterapp.personal.entity.cref.Cref;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.exception.IncompleteProfileException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;
import com.fitterapp.user.entity.User;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SubmitProfileForReviewServiceTests {
  private static final Instant NOW = Instant.parse("2026-07-27T20:00:00Z");
  @Mock ProfileRepository profiles;
  @Mock RevisionModalityRepository modalities;
  @Mock RevisionServiceModeRepository modes;
  @Mock RevisionServiceAreaRepository areas;
  @Mock FunnelEventRepository funnelEvents;

  @Test
  void submitsCompleteDraftWithoutCref() {
    Fixture f = fixture(true, false);
    stub(f, 1, 1, 1);
    var result = service().submit(new SubmitProfileForReviewCommand(f.userId, f.profileId));
    assertThat(f.revision.getStatus()).isEqualTo(ProfileRevisionStatus.PENDING_REVIEW);
    assertThat(f.revision.getCref()).isNull();
    assertThat(result.revisionId()).isEqualTo(f.revisionId);
    var captor = org.mockito.ArgumentCaptor.forClass(FunnelEvent.class);
    verify(funnelEvents).save(captor.capture());
    assertThat(captor.getValue().getEventType()).isEqualTo(FunnelEventType.PROFILE_SUBMITTED);
    assertThat(captor.getValue().getPersonalProfile()).isSameAs(f.profile);
  }

  @Test
  void submitsCompleteDraftWithCref() {
    Fixture f = fixture(true, true);
    stub(f, 1, 1, 1);

    service().submit(new SubmitProfileForReviewCommand(f.userId, f.profileId));

    assertThat(f.revision.getStatus()).isEqualTo(ProfileRevisionStatus.PENDING_REVIEW);
    assertThat(f.revision.getCref()).isNotNull();
    assertThat(f.revision.getCref().getDocumentImageKey()).isEqualTo("private/cref.webp");
  }

  @Test
  void rejectsMissingRequiredFieldEvenWithoutCref() {
    Fixture f = fixture(false, false);
    when(profiles.findByIdAndUserId(f.profileId, f.userId)).thenReturn(Optional.of(f.profile));
    assertThatThrownBy(
            () -> service().submit(new SubmitProfileForReviewCommand(f.userId, f.profileId)))
        .isInstanceOf(IncompleteProfileException.class);
    assertThat(f.revision.getStatus()).isEqualTo(ProfileRevisionStatus.DRAFT);
    verify(funnelEvents, never()).save(any());
  }

  private void stub(Fixture f, long modalityCount, long modeCount, long areaCount) {
    when(profiles.findByIdAndUserId(f.profileId, f.userId)).thenReturn(Optional.of(f.profile));
    when(modalities.countByIdRevisionId(f.revisionId)).thenReturn(modalityCount);
    when(modes.countByIdRevisionId(f.revisionId)).thenReturn(modeCount);
    when(areas.countByRevisionId(f.revisionId)).thenReturn(areaCount);
  }

  private SubmitProfileForReviewService service() {
    return new SubmitProfileForReviewService(
        profiles, modalities, modes, areas, Clock.fixed(NOW, ZoneOffset.UTC), funnelEvents);
  }

  private Fixture fixture(boolean complete, boolean withCref) {
    UUID userId = UUID.randomUUID(), profileId = UUID.randomUUID(), revisionId = UUID.randomUUID();
    User user =
        User.pendingRegistration(
            "Bruno", "bruno@fitterapp.com", "+5544999999999", "hash", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(user, "id", userId);
    Profile profile = Profile.draft("Bruno", "bruno", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(profile, "id", profileId);
    profile.linkUser(user, NOW.atOffset(ZoneOffset.UTC));
    ProfileRevision revision =
        ProfileRevision.draft(profile, 1, user, true, NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(revision, "id", revisionId);
    revision.updateProfessionalData(
        "Bruno",
        complete ? "Bio" : null,
        "+5544999999999",
        null,
        null,
        null,
        NOW.atOffset(ZoneOffset.UTC));
    if (withCref)
      revision.assignCref(
          Cref.pendingReview(
              profile, "CREF-PR-1", "private/cref.webp", NOW.atOffset(ZoneOffset.UTC)),
          NOW.atOffset(ZoneOffset.UTC));
    profile.setCurrentRevision(revision, NOW.atOffset(ZoneOffset.UTC));
    return new Fixture(userId, profileId, revisionId, profile, revision);
  }

  private record Fixture(
      UUID userId, UUID profileId, UUID revisionId, Profile profile, ProfileRevision revision) {}
}
