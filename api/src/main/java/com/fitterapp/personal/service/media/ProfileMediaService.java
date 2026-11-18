package com.fitterapp.personal.service.media;

import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.service.cref.UpsertCrefCommand;
import com.fitterapp.personal.service.cref.UpsertCrefService;
import com.fitterapp.storage.ImageStorage;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileMediaService {
  private final ProfileRepository profiles;
  private final ImageStorage storage;
  private final UpsertCrefService crefs;
  private final Clock clock;

  @Transactional
  public String uploadPhoto(UUID userId, UUID profileId, MultipartFile image) {
    var profile = editable(userId, profileId);
    String key = storage.store(image, "personals", profileId);
    rollbackDelete(key);
    profile.getCurrentRevision().updateProfileImage(key, OffsetDateTime.now(clock));
    return key;
  }

  @Transactional
  public String uploadCref(
      UUID userId, UUID profileId, String registrationCode, MultipartFile image) {
    var profile = editable(userId, profileId);
    if (registrationCode == null
        || registrationCode.isBlank()
        || registrationCode.trim().length() > 40)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um CREF válido");
    if (profile.getPublishedRevision() != null && profile.getCurrentRevision().getCref() != null)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "A alteração do CREF em perfil publicado ainda não está disponível");
    String key = storage.store(image, "private/crefs", profileId);
    rollbackDelete(key);
    crefs.upsert(new UpsertCrefCommand(userId, profileId, registrationCode, key));
    return key;
  }

  @Transactional(readOnly = true)
  public byte[] publicPhoto(UUID profileId, UUID imageId) {
    var profile = profiles.findById(profileId).orElseThrow(ProfileNotFoundException::new);
    String key = "personals/" + profileId + "/" + imageId + ".webp";
    if (profile.getStatus() != ProfileStatus.PUBLISHED
        || profile.getPublishedRevision() == null
        || !key.equals(profile.getPublishedRevision().getProfileImageKey()))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada");
    return storage.read(key);
  }

  @Transactional(readOnly = true)
  public byte[] adminPhoto(UUID profileId) {
    var profile = profiles.findById(profileId).orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();
    String key = revision == null ? null : revision.getProfileImageKey();
    if (key == null || !key.startsWith("personals/" + profileId + "/"))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada");
    return storage.read(key);
  }

  @Transactional(readOnly = true)
  public byte[] adminCref(UUID profileId) {
    var profile = profiles.findById(profileId).orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();
    if (revision == null || revision.getCref() == null)
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado");
    String key = revision.getCref().getDocumentImageKey();
    if (key == null || !key.startsWith("private/crefs/" + profileId + "/"))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado");
    return storage.read(key);
  }

  private com.fitterapp.personal.entity.profile.Profile editable(UUID userId, UUID profileId) {
    var profile =
        profiles.findByIdAndUserId(profileId, userId).orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();
    if (profile.getStatus() == ProfileStatus.SUSPENDED
        || revision == null
        || (revision.getStatus() != ProfileRevisionStatus.DRAFT
            && revision.getStatus() != ProfileRevisionStatus.REJECTED))
      throw new ProfileRevisionNotEditableException();
    return profile;
  }

  private void rollbackDelete(String key) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCompletion(int status) {
            if (status != STATUS_COMMITTED)
              try {
                storage.delete(key);
              } catch (RuntimeException e) {
                log.error("Failed to remove image after rollback", e);
              }
          }
        });
  }
}
