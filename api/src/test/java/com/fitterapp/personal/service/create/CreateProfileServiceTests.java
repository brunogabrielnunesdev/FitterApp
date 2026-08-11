package com.fitterapp.personal.service.create;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.analytics.entity.event.FunnelEvent;
import com.fitterapp.analytics.entity.event.FunnelEventType;
import com.fitterapp.analytics.repository.FunnelEventRepository;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.exception.ProfileAlreadyExistsException;
import com.fitterapp.personal.exception.ProfileApplicantNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.ProfileRevisionRepository;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CreateProfileServiceTests {

  private static final Instant NOW = Instant.parse("2026-07-27T15:00:00Z");

  @Mock private UserRepository userRepository;

  @Mock private ProfileRepository profileRepository;

  @Mock private ProfileRevisionRepository profileRevisionRepository;

  @Mock private FunnelEventRepository funnelEvents;

  private CreateProfileService service;

  @BeforeEach
  void setUp() {
    service =
        new CreateProfileService(
            userRepository,
            profileRepository,
            profileRevisionRepository,
            new ProfileSlugGenerator(),
            Clock.fixed(NOW, ZoneOffset.UTC),
            funnelEvents);
  }

  @Test
  void createsLinkedProfileAndFirstDraftRevision() {
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
    when(profileRepository.existsByUserId(userId)).thenReturn(false);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(profileRepository.save(any(Profile.class)))
        .thenAnswer(
            invocation -> {
              Profile profile = invocation.getArgument(0);
              ReflectionTestUtils.setField(profile, "id", profileId);
              return profile;
            });
    when(profileRevisionRepository.save(any(ProfileRevision.class)))
        .thenAnswer(
            invocation -> {
              ProfileRevision revision = invocation.getArgument(0);
              ReflectionTestUtils.setField(revision, "id", revisionId);
              return revision;
            });

    CreateProfileResult result = service.create(new CreateProfileCommand(userId));

    ArgumentCaptor<Profile> profileCaptor = ArgumentCaptor.forClass(Profile.class);
    ArgumentCaptor<ProfileRevision> revisionCaptor = ArgumentCaptor.forClass(ProfileRevision.class);
    verify(profileRepository).save(profileCaptor.capture());
    verify(profileRevisionRepository).save(revisionCaptor.capture());
    ArgumentCaptor<FunnelEvent> funnelCaptor = ArgumentCaptor.forClass(FunnelEvent.class);
    verify(funnelEvents).save(funnelCaptor.capture());

    Profile profile = profileCaptor.getValue();
    ProfileRevision revision = revisionCaptor.getValue();
    assertThat(profile.getUser()).isSameAs(user);
    assertThat(profile.getFullName()).isEqualTo("Bruno Gabriel");
    assertThat(profile.getSlug()).matches("bruno-gabriel-[0-9a-f]{8}");
    assertThat(profile.getStatus()).isEqualTo(ProfileStatus.DRAFT);
    assertThat(profile.getCurrentRevision()).isSameAs(revision);
    assertThat(revision.getPersonal()).isSameAs(profile);
    assertThat(revision.getVersionNumber()).isEqualTo(1);
    assertThat(revision.getCreatedBy()).isSameAs(user);
    assertThat(revision.isRequiresReview()).isTrue();
    assertThat(revision.getStatus()).isEqualTo(ProfileRevisionStatus.DRAFT);
    assertThat(result).isEqualTo(new CreateProfileResult(profileId, revisionId));
    assertThat(funnelCaptor.getValue().getEventType()).isEqualTo(FunnelEventType.PROFILE_STARTED);
    assertThat(funnelCaptor.getValue().getUser()).isSameAs(user);
    assertThat(funnelCaptor.getValue().getPersonalProfile()).isSameAs(profile);
  }

  @Test
  void rejectsUserThatAlreadyHasProfile() {
    UUID userId = UUID.randomUUID();
    when(profileRepository.existsByUserId(userId)).thenReturn(true);

    assertThatThrownBy(() -> service.create(new CreateProfileCommand(userId)))
        .isInstanceOf(ProfileAlreadyExistsException.class);

    verify(userRepository, never()).findById(any());
    verify(profileRepository, never()).save(any());
    verify(profileRevisionRepository, never()).save(any());
    verify(funnelEvents, never()).save(any());
  }

  @Test
  void rejectsMissingApplicant() {
    UUID userId = UUID.randomUUID();
    when(profileRepository.existsByUserId(userId)).thenReturn(false);
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(new CreateProfileCommand(userId)))
        .isInstanceOf(ProfileApplicantNotFoundException.class);

    verify(profileRepository, never()).save(any());
    verify(profileRevisionRepository, never()).save(any());
    verify(funnelEvents, never()).save(any());
  }
}
