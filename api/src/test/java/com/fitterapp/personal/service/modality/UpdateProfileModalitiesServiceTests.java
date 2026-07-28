package com.fitterapp.personal.service.modality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.exception.UnavailableModalityException;
import com.fitterapp.personal.repository.ModalityRepository;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import com.fitterapp.user.entity.User;
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
class UpdateProfileModalitiesServiceTests {

  private static final Instant NOW = Instant.parse("2026-07-27T18:00:00Z");

  @Mock private ProfileRepository profileRepository;

  @Mock private ModalityRepository modalityRepository;

  @Mock private RevisionModalityRepository revisionModalityRepository;

  @Test
  void replacesCurrentRevisionModalitiesWithActiveSelection() {
    Fixture fixture = fixture();
    Modality bodybuilding = modality((short) 1);
    Modality running = modality((short) 2);
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));
    when(modalityRepository.findAllByIdInAndActiveTrue(anyCollection()))
        .thenReturn(List.of(bodybuilding, running));

    UpdateProfileModalitiesResult result =
        service()
            .update(
                new UpdateProfileModalitiesCommand(
                    fixture.userId, fixture.profileId, List.of((short) 1, (short) 2, (short) 1)));

    verify(revisionModalityRepository).deleteByRevisionId(fixture.revisionId);
    ArgumentCaptor<Iterable<RevisionModality>> linksCaptor = revisionModalitiesCaptor();
    verify(revisionModalityRepository).saveAll(linksCaptor.capture());
    assertThat(linksCaptor.getValue())
        .extracting(link -> link.getModality().getId())
        .containsExactly((short) 1, (short) 2);
    assertThat(linksCaptor.getValue())
        .allSatisfy(link -> assertThat(link.getRevision()).isSameAs(fixture.revision));
    assertThat(result)
        .isEqualTo(
            new UpdateProfileModalitiesResult(
                fixture.profileId, fixture.revisionId, List.of((short) 1, (short) 2)));
  }

  @Test
  void rejectsUnavailableModalitiesWithoutChangingCurrentSelection() {
    Fixture fixture = fixture();
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));
    when(modalityRepository.findAllByIdInAndActiveTrue(anyCollection())).thenReturn(List.of());

    assertThatThrownBy(
            () ->
                service()
                    .update(
                        new UpdateProfileModalitiesCommand(
                            fixture.userId, fixture.profileId, List.of((short) 1, (short) 2))))
        .isInstanceOf(UnavailableModalityException.class);

    verify(revisionModalityRepository, never()).deleteByRevisionId(any());
    verify(revisionModalityRepository, never()).saveAll(any());
  }

  @Test
  void allowsUpdatingRejectedRevision() {
    Fixture fixture = fixture();
    fixture.revision.submit(NOW.atOffset(ZoneOffset.UTC));
    fixture.revision.reject(fixture.user, "Ajuste suas modalidades", NOW.atOffset(ZoneOffset.UTC));
    Modality bodybuilding = modality((short) 1);
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));
    when(modalityRepository.findAllByIdInAndActiveTrue(anyCollection()))
        .thenReturn(List.of(bodybuilding));

    service()
        .update(
            new UpdateProfileModalitiesCommand(
                fixture.userId, fixture.profileId, List.of((short) 1)));

    verify(revisionModalityRepository).deleteByRevisionId(fixture.revisionId);
  }

  @Test
  void rejectsUpdatingRevisionAwaitingReview() {
    Fixture fixture = fixture();
    fixture.revision.submit(NOW.atOffset(ZoneOffset.UTC));
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));

    assertThatThrownBy(
            () ->
                service()
                    .update(
                        new UpdateProfileModalitiesCommand(
                            fixture.userId, fixture.profileId, List.of((short) 1))))
        .isInstanceOf(ProfileRevisionNotEditableException.class);

    verify(modalityRepository, never()).findAllByIdInAndActiveTrue(any());
  }

  private UpdateProfileModalitiesService service() {
    return new UpdateProfileModalitiesService(
        profileRepository, modalityRepository, revisionModalityRepository);
  }

  private Fixture fixture() {
    UUID userId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
    UUID revisionId = UUID.randomUUID();
    User user =
        User.pendingRegistration(
            "Bruno Gabriel",
            "bruno@fitterapp.com",
            "+5544999999999",
            "password-hash",
            NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(user, "id", userId);
    Profile profile = Profile.draft("Bruno Gabriel", "bruno-gabriel", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(profile, "id", profileId);
    profile.linkUser(user, NOW.atOffset(ZoneOffset.UTC));
    ProfileRevision revision =
        ProfileRevision.draft(profile, 1, user, true, NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(revision, "id", revisionId);
    profile.setCurrentRevision(revision, NOW.atOffset(ZoneOffset.UTC));
    return new Fixture(userId, profileId, revisionId, user, profile, revision);
  }

  private Modality modality(short id) {
    Modality modality = mock(Modality.class);
    when(modality.getId()).thenReturn(id);
    return modality;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ArgumentCaptor<Iterable<RevisionModality>> revisionModalitiesCaptor() {
    return (ArgumentCaptor) ArgumentCaptor.forClass(Iterable.class);
  }

  private record Fixture(
      UUID userId,
      UUID profileId,
      UUID revisionId,
      User user,
      Profile profile,
      ProfileRevision revision) {}
}
