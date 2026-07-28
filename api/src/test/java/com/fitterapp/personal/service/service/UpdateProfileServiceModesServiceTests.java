package com.fitterapp.personal.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;
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
class UpdateProfileServiceModesServiceTests {

  private static final Instant NOW = Instant.parse("2026-07-27T18:30:00Z");

  @Mock private ProfileRepository profileRepository;

  @Mock private RevisionServiceModeRepository revisionServiceModeRepository;

  @Test
  void replacesCurrentRevisionServiceModes() {
    Fixture fixture = fixture();
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));

    UpdateProfileServiceModesResult result =
        service()
            .update(
                new UpdateProfileServiceModesCommand(
                    fixture.userId,
                    fixture.profileId,
                    List.of(ServiceMode.IN_PERSON, ServiceMode.ONLINE, ServiceMode.IN_PERSON)));

    verify(revisionServiceModeRepository).deleteByRevisionId(fixture.revisionId);
    ArgumentCaptor<Iterable<RevisionServiceMode>> modesCaptor = revisionServiceModesCaptor();
    verify(revisionServiceModeRepository).saveAll(modesCaptor.capture());
    assertThat(modesCaptor.getValue())
        .extracting(RevisionServiceMode::getServiceMode)
        .containsExactly(ServiceMode.IN_PERSON, ServiceMode.ONLINE);
    assertThat(result)
        .isEqualTo(
            new UpdateProfileServiceModesResult(
                fixture.profileId,
                fixture.revisionId,
                List.of(ServiceMode.IN_PERSON, ServiceMode.ONLINE)));
  }

  @Test
  void clearsServiceModesWhenSelectionIsEmpty() {
    Fixture fixture = fixture();
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));

    UpdateProfileServiceModesResult result =
        service()
            .update(
                new UpdateProfileServiceModesCommand(fixture.userId, fixture.profileId, List.of()));

    verify(revisionServiceModeRepository).deleteByRevisionId(fixture.revisionId);
    verify(revisionServiceModeRepository, never()).saveAll(any());
    assertThat(result.serviceModes()).isEmpty();
  }

  @Test
  void allowsUpdatingRejectedRevision() {
    Fixture fixture = fixture();
    fixture.revision.submit(NOW.atOffset(ZoneOffset.UTC));
    fixture.revision.reject(
        fixture.user, "Corrija a forma de atendimento", NOW.atOffset(ZoneOffset.UTC));
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));

    service()
        .update(
            new UpdateProfileServiceModesCommand(
                fixture.userId, fixture.profileId, List.of(ServiceMode.HOME_VISIT)));

    verify(revisionServiceModeRepository).deleteByRevisionId(fixture.revisionId);
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
                        new UpdateProfileServiceModesCommand(
                            fixture.userId, fixture.profileId, List.of(ServiceMode.ONLINE))))
        .isInstanceOf(ProfileRevisionNotEditableException.class);

    verify(revisionServiceModeRepository, never()).deleteByRevisionId(any());
  }

  private UpdateProfileServiceModesService service() {
    return new UpdateProfileServiceModesService(profileRepository, revisionServiceModeRepository);
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

  @SuppressWarnings({"unchecked", "rawtypes"})
  private ArgumentCaptor<Iterable<RevisionServiceMode>> revisionServiceModesCaptor() {
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
