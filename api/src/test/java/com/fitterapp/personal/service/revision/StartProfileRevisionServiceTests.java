package com.fitterapp.personal.service.revision;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.ProfileRevisionRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;
import com.fitterapp.user.entity.User;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
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
class StartProfileRevisionServiceTests {
  @Mock ProfileRepository profiles;
  @Mock ProfileRevisionRepository revisions;
  @Mock RevisionModalityRepository modalities;
  @Mock RevisionServiceModeRepository modes;
  @Mock RevisionServiceAreaRepository areas;

  @Test
  void copiesApprovedRevisionWithoutRemovingPublishedVersion() {
    OffsetDateTime now = OffsetDateTime.parse("2026-08-04T18:00:00Z");
    UUID userId = UUID.randomUUID(), profileId = UUID.randomUUID(), sourceId = UUID.randomUUID();
    User user = User.pendingRegistration("Bruno", "bruno@test.com", "+5544999999999", "hash", now.minusDays(2));
    ReflectionTestUtils.setField(user, "id", userId);
    Profile profile = Profile.draft("Bruno", "bruno", now.minusDays(2));
    ReflectionTestUtils.setField(profile, "id", profileId);
    profile.linkUser(user, now.minusDays(2));
    ProfileRevision source = ProfileRevision.draft(profile, 1, user, true, now.minusDays(2));
    ReflectionTestUtils.setField(source, "id", sourceId);
    source.updateProfessionalData("Bruno Personal", "Bio", "+5544999999999", (short) 2020, "CREF", "Academia", now.minusDays(1));
    source.submit(now.minusDays(1));
    source.approve(user, now.minusHours(12));
    profile.publish(source, now.minusHours(12));
    when(profiles.findByIdAndUserId(profileId, userId)).thenReturn(Optional.of(profile));
    when(modalities.findAllByRevisionIdOrderByModalityNameAsc(sourceId)).thenReturn(List.of());
    when(modes.findAllByRevisionIdOrderByIdServiceModeAsc(sourceId)).thenReturn(List.of());
    when(areas.findAllByRevisionIdOrderByCityAscNeighborhoodAsc(sourceId)).thenReturn(List.of());

    var service = new StartProfileRevisionService(profiles, revisions, modalities, modes, areas, Clock.fixed(Instant.from(now), ZoneOffset.UTC));
    service.start(new StartProfileRevisionCommand(userId, profileId));

    ArgumentCaptor<ProfileRevision> captor = ArgumentCaptor.forClass(ProfileRevision.class);
    verify(revisions).save(captor.capture());
    ProfileRevision draft = captor.getValue();
    assertThat(draft.getVersionNumber()).isEqualTo(2);
    assertThat(draft.getStatus()).isEqualTo(ProfileRevisionStatus.DRAFT);
    assertThat(draft.getFullName()).isEqualTo(source.getFullName());
    assertThat(profile.getCurrentRevision()).isSameAs(draft);
    assertThat(profile.getPublishedRevision()).isSameAs(source);
    assertThat(profile.isPublished()).isTrue();
    verify(modalities).saveAll(any());
    verify(modes).saveAll(any());
    verify(areas).saveAll(any());
  }
}
