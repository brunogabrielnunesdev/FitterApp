package com.fitterapp.personal.service.media;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.personal.exception.*;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.service.cref.UpsertCrefService;
import com.fitterapp.storage.ImageStorage;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

class ProfileMediaServiceTests {
  private final ProfileRepository profiles = mock(ProfileRepository.class);
  private final ImageStorage storage = mock(ImageStorage.class);
  private final UpsertCrefService cref = mock(UpsertCrefService.class);
  private final UUID userId = UUID.randomUUID(),
      profileId = UUID.randomUUID(),
      imageId = UUID.randomUUID();
  private final OffsetDateTime now = OffsetDateTime.parse("2026-11-18T12:00:00Z");
  private final String key = "personals/" + profileId + "/" + imageId + ".webp";

  private ProfileMediaService service() {
    return new ProfileMediaService(
        profiles, storage, cref, Clock.fixed(now.toInstant(), ZoneOffset.UTC));
  }

  @Test
  void uploadsOwnedDraftAndDeletesFileOnRollback() {
    var profile = Profile.draft("Ana", "ana", now);
    var revision = ProfileRevision.draft(profile, 1, null, true, now);
    profile.setCurrentRevision(revision, now);
    when(profiles.findByIdAndUserId(profileId, userId)).thenReturn(Optional.of(profile));
    when(storage.store(any(), eq("personals"), eq(profileId))).thenReturn(key);
    TransactionSynchronizationManager.initSynchronization();
    try {
      assertThat(
              service()
                  .uploadPhoto(userId, profileId, new MockMultipartFile("file", new byte[] {1})))
          .isEqualTo(key);
      assertThat(revision.getProfileImageKey()).isEqualTo(key);
      TransactionSynchronizationManager.getSynchronizations()
          .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
      verify(storage).delete(key);
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  void refusesForeignProfile() {
    when(profiles.findByIdAndUserId(profileId, userId)).thenReturn(Optional.empty());
    assertThatThrownBy(
            () ->
                service()
                    .uploadPhoto(userId, profileId, new MockMultipartFile("file", new byte[] {1})))
        .isInstanceOf(ProfileNotFoundException.class);
    verifyNoInteractions(storage);
  }

  @Test
  void doesNotExposeUnpublishedPhoto() {
    var profile = Profile.draft("Ana", "ana", now);
    var revision = ProfileRevision.draft(profile, 1, null, true, now);
    revision.updateProfileImage(key, now);
    profile.setCurrentRevision(revision, now);
    when(profiles.findById(profileId)).thenReturn(Optional.of(profile));
    assertThatThrownBy(() -> service().publicPhoto(profileId, imageId))
        .isInstanceOf(ResponseStatusException.class);
    verifyNoInteractions(storage);
    profile.publish(revision, now);
    when(storage.read(key)).thenReturn(new byte[] {1});
    assertThat(service().publicPhoto(profileId, imageId)).containsExactly((byte) 1);
  }
}
