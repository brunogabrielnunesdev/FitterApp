package com.fitterapp.personal.service.management;

import com.fitterapp.analytics.domain.AdminAuditLogActions;
import com.fitterapp.analytics.entity.audit.AuditTargetType;
import com.fitterapp.auth.service.register.*;
import com.fitterapp.personal.controller.AdminPersonalManagementController.*;
import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.service.create.*;
import com.fitterapp.personal.service.cref.*;
import com.fitterapp.personal.service.modality.*;
import com.fitterapp.personal.service.revision.*;
import com.fitterapp.personal.service.service.*;
import com.fitterapp.personal.service.update.*;
import com.fitterapp.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminPersonalManagementService {
  private final RegisterService register;
  private final CreateProfileService createProfile;
  private final StartProfileRevisionService revisions;
  private final UpdateProfileDraftService draft;
  private final UpdateProfileModalitiesService modalities;
  private final UpdateProfileServiceModesService modes;
  private final UpdateProfileServiceAreasService areas;
  private final UpsertCrefService cref;
  private final ProfileRepository profiles;
  private final UserRepository users;
  private final EntityManager entityManager;
  private final Clock clock;

  @Transactional
  public Result create(UUID adminId, CreateRequest request) {
    var actor =
        users
            .findById(adminId)
            .orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "Administrador não encontrado"));
    var account =
        register.register(
            new RegisterCommand(
                request.accountFullName(),
                request.email(),
                request.phoneNumber(),
                request.temporaryPassword()));
    var created = createProfile.create(new CreateProfileCommand(account.userId()));
    apply(account.userId(), created.profileId(), request.profile(), true);
    entityManager.persist(
        AdminAuditLogActions.record(
            actor,
            "PERSONAL_CREATED",
            AuditTargetType.PERSONAL_PROFILE,
            created.profileId(),
            request.reason().trim(),
            null,
            null,
            null,
            OffsetDateTime.now(clock)));
    return new Result(account.userId(), created.profileId(), created.revisionId());
  }

  @Transactional
  public Result update(UUID adminId, UUID profileId, UpdateRequest request) {
    var actor =
        users
            .findById(adminId)
            .orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "Administrador não encontrado"));
    var profile =
        profiles
            .findByIdForUpdate(profileId)
            .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Perfil não encontrado"));
    if (profile.getUser() == null || profile.getCurrentRevision() == null)
      throw error(HttpStatus.CONFLICT, "Perfil sem conta ou revisão");
    if (profile.getStatus() == ProfileStatus.SUSPENDED)
      throw error(HttpStatus.CONFLICT, "Perfil suspenso não pode ser editado");
    var userId = profile.getUser().getId();
    var current = profile.getCurrentRevision();
    var existingCref = current.getCref();
    var requestedCref = request.profile().cref();
    boolean crefUnchanged =
        existingCref != null
            && requestedCref != null
            && existingCref
                .getRegistrationCode()
                .equalsIgnoreCase(requestedCref.registrationCode().trim())
            && existingCref.getDocumentImageKey().equals(requestedCref.documentImageKey());
    if (existingCref != null && requestedCref == null)
      throw error(HttpStatus.CONFLICT, "A remoção do CREF ainda não está disponível");
    if (profile.isPublished() && existingCref != null && !crefUnchanged)
      throw error(
          HttpStatus.CONFLICT, "A alteração do CREF em perfil publicado ainda não está disponível");
    if (current.getStatus() == ProfileRevisionStatus.APPROVED) {
      revisions.start(new StartProfileRevisionCommand(userId, profileId));
    } else if (current.getStatus() != ProfileRevisionStatus.DRAFT
        && current.getStatus() != ProfileRevisionStatus.REJECTED)
      throw error(HttpStatus.CONFLICT, "A revisão não pode ser editada neste estado");
    apply(userId, profileId, request.profile(), !crefUnchanged);
    entityManager.persist(
        AdminAuditLogActions.record(
            actor,
            "PERSONAL_UPDATED",
            AuditTargetType.PERSONAL_PROFILE,
            profileId,
            request.reason().trim(),
            null,
            null,
            null,
            OffsetDateTime.now(clock)));
    return new Result(userId, profileId, profile.getCurrentRevision().getId());
  }

  private void apply(UUID userId, UUID profileId, ProfileInput input, boolean updateCref) {
    draft.update(
        new UpdateProfileDraftCommand(
            userId,
            profileId,
            input.fullName(),
            input.biography(),
            input.whatsapp(),
            input.experienceStartedYear(),
            input.certifications(),
            input.gymsDescription(),
            input.startingPriceCents(),
            input.priceUnit()));
    modalities.update(new UpdateProfileModalitiesCommand(userId, profileId, input.modalityIds()));
    modes.update(new UpdateProfileServiceModesCommand(userId, profileId, input.serviceModes()));
    areas.update(new UpdateProfileServiceAreasCommand(userId, profileId, input.serviceAreas()));
    if (updateCref && input.cref() != null)
      cref.upsert(
          new UpsertCrefCommand(
              userId, profileId, input.cref().registrationCode(), input.cref().documentImageKey()));
  }

  private ResponseStatusException error(HttpStatus status, String message) {
    return new ResponseStatusException(status, message);
  }

  public record Result(UUID userId, UUID profileId, UUID revisionId) {}
}
