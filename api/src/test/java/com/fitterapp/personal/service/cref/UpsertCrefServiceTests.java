package com.fitterapp.personal.service.cref;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.cref.Cref;
import com.fitterapp.personal.entity.cref.CrefStatus;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.exception.CrefAlreadyInUseException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.CrefRepository;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.entity.User;
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
class UpsertCrefServiceTests {

  private static final Instant NOW = Instant.parse("2026-07-27T17:00:00Z");

  @Mock private ProfileRepository profileRepository;

  @Mock private CrefRepository crefRepository;

  private UpsertCrefService service;

  @BeforeEach
  void setUp() {
    service =
        new UpsertCrefService(profileRepository, crefRepository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void createsPendingCrefAndAssignsItToCurrentRevision() {
    Fixture fixture = fixture();
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));
    when(crefRepository.findByRegistrationCode("CREF-PR-12345")).thenReturn(Optional.empty());
    when(crefRepository.findByPersonalId(fixture.profileId)).thenReturn(Optional.empty());
    when(crefRepository.save(any(Cref.class)))
        .thenAnswer(
            invocation -> {
              Cref cref = invocation.getArgument(0);
              ReflectionTestUtils.setField(cref, "id", fixture.crefId);
              return cref;
            });

    UpsertCrefResult result = service.upsert(command(fixture));

    ArgumentCaptor<Cref> crefCaptor = ArgumentCaptor.forClass(Cref.class);
    verify(crefRepository).save(crefCaptor.capture());
    Cref cref = crefCaptor.getValue();
    assertThat(cref.getPersonal()).isSameAs(fixture.profile);
    assertThat(cref.getRegistrationCode()).isEqualTo("CREF-PR-12345");
    assertThat(cref.getDocumentImageKey()).isEqualTo("private/crefs/a/b.webp");
    assertThat(cref.getStatus()).isEqualTo(CrefStatus.PENDING_REVIEW);
    assertThat(fixture.revision.getCref()).isSameAs(cref);
    assertThat(result).isEqualTo(new UpsertCrefResult(fixture.profileId, fixture.crefId));
  }

  @Test
  void resubmitsExistingRejectedCref() {
    Fixture fixture = fixture();
    Cref cref =
        Cref.pendingReview(
            fixture.profile,
            "CREF-PR-OLD",
            "private/crefs/a/old.webp",
            NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(cref, "id", fixture.crefId);
    cref.reject(fixture.user, "Documento ilegivel", NOW.atOffset(ZoneOffset.UTC));
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));
    when(crefRepository.findByRegistrationCode("CREF-PR-12345")).thenReturn(Optional.empty());
    when(crefRepository.findByPersonalId(fixture.profileId)).thenReturn(Optional.of(cref));

    service.upsert(command(fixture));

    assertThat(cref.getRegistrationCode()).isEqualTo("CREF-PR-12345");
    assertThat(cref.getDocumentImageKey()).isEqualTo("private/crefs/a/b.webp");
    assertThat(cref.getStatus()).isEqualTo(CrefStatus.PENDING_REVIEW);
    assertThat(cref.getVerifiedBy()).isNull();
    assertThat(cref.getRejectionReason()).isNull();
    assertThat(fixture.revision.getCref()).isSameAs(cref);
    verify(crefRepository, never()).save(any());
  }

  @Test
  void rejectsCrefOwnedByAnotherProfile() {
    Fixture fixture = fixture();
    Profile otherProfile =
        Profile.draft("Other Personal", "other-personal", NOW.atOffset(ZoneOffset.UTC));
    ReflectionTestUtils.setField(otherProfile, "id", UUID.randomUUID());
    Cref existing =
        Cref.pendingReview(
            otherProfile,
            "CREF-PR-12345",
            "private/crefs/a/other.webp",
            NOW.atOffset(ZoneOffset.UTC));
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));
    when(crefRepository.findByRegistrationCode("CREF-PR-12345")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.upsert(command(fixture)))
        .isInstanceOf(CrefAlreadyInUseException.class);

    verify(crefRepository, never()).findByPersonalId(any());
    verify(crefRepository, never()).save(any());
  }

  @Test
  void rejectsCrefEditWhileRevisionIsInReview() {
    Fixture fixture = fixture();
    fixture.revision.submit(NOW.atOffset(ZoneOffset.UTC));
    when(profileRepository.findByIdAndUserId(fixture.profileId, fixture.userId))
        .thenReturn(Optional.of(fixture.profile));

    assertThatThrownBy(() -> service.upsert(command(fixture)))
        .isInstanceOf(ProfileRevisionNotEditableException.class);

    verify(crefRepository, never()).findByRegistrationCode(any());
  }

  private UpsertCrefCommand command(Fixture fixture) {
    return new UpsertCrefCommand(
        fixture.userId, fixture.profileId, "  cref-pr-12345 ", "private/crefs/a/b.webp");
  }

  private Fixture fixture() {
    UUID userId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();
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
    ReflectionTestUtils.setField(revision, "id", UUID.randomUUID());
    profile.setCurrentRevision(revision, NOW.atOffset(ZoneOffset.UTC));
    return new Fixture(userId, profileId, UUID.randomUUID(), user, profile, revision);
  }

  private record Fixture(
      UUID userId,
      UUID profileId,
      UUID crefId,
      User user,
      Profile profile,
      ProfileRevision revision) {}
}
