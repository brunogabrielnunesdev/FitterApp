package com.fitterapp.personal.service.admin;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.entity.audit.AdminAuditLog;
import com.fitterapp.analytics.entity.audit.AuditTargetType;
import com.fitterapp.analytics.repository.AdminAuditLogRepository;
import com.fitterapp.auth.service.register.RegisterCommand;
import com.fitterapp.auth.service.register.RegisterService;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.service.create.CreateProfileCommand;
import com.fitterapp.personal.service.create.CreateProfileService;
import com.fitterapp.personal.service.cref.UpsertCrefCommand;
import com.fitterapp.personal.service.cref.UpsertCrefService;
import com.fitterapp.personal.service.modality.UpdateProfileModalitiesCommand;
import com.fitterapp.personal.service.modality.UpdateProfileModalitiesService;
import com.fitterapp.personal.service.service.UpdateProfileServiceAreasCommand;
import com.fitterapp.personal.service.service.UpdateProfileServiceAreasService;
import com.fitterapp.personal.service.service.UpdateProfileServiceModesCommand;
import com.fitterapp.personal.service.service.UpdateProfileServiceModesService;
import com.fitterapp.personal.service.update.UpdateProfileDraftCommand;
import com.fitterapp.personal.service.update.UpdateProfileDraftService;
import com.fitterapp.user.exception.UserNotFoundException;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPersonalManagementService {
  private static final String CREATED_ACTION = "PERSONAL_PROFILE_ADMIN_CREATED";
  private static final String UPDATED_ACTION = "PERSONAL_PROFILE_ADMIN_UPDATED";

  private final RegisterService registerService;
  private final CreateProfileService createProfileService;
  private final UpdateProfileDraftService updateProfileDraftService;
  private final UpdateProfileModalitiesService updateProfileModalitiesService;
  private final UpdateProfileServiceModesService updateProfileServiceModesService;
  private final UpdateProfileServiceAreasService updateProfileServiceAreasService;
  private final UpsertCrefService upsertCrefService;
  private final ProfileRepository profiles;
  private final UserRepository users;
  private final AdminAuditLogRepository auditLogs;
  private final Clock clock;

  @Transactional
  public AdminPersonalResult create(AdminCreatePersonalCommand command) {
    var admin = users.findById(command.adminUserId()).orElseThrow(UserNotFoundException::new);
    var registration =
        registerService.register(
            new RegisterCommand(
                command.accountFullName(),
                command.email(),
                command.phoneNumber(),
                command.temporaryPassword(),
                EventSource.ADMIN_WEB));
    var created =
        createProfileService.create(
            new CreateProfileCommand(registration.userId(), EventSource.ADMIN_WEB));
    apply(registration.userId(), created.profileId(), command.profile());
    OffsetDateTime now = OffsetDateTime.now(clock);
    auditLogs.save(
        AdminAuditLog.record(
            admin,
            CREATED_ACTION,
            AuditTargetType.PERSONAL_PROFILE,
            created.profileId(),
            command.reason().trim(),
            null,
            null,
            state(registration.userId(), created.revisionId()),
            now));
    return new AdminPersonalResult(
        registration.userId(), created.profileId(), created.revisionId());
  }

  @Transactional
  public AdminPersonalResult update(AdminUpdatePersonalCommand command) {
    var admin = users.findById(command.adminUserId()).orElseThrow(UserNotFoundException::new);
    var profile = profiles.findById(command.profileId()).orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();
    if (revision == null) throw new ProfileNotFoundException();
    var previousState = state(profile.getUser().getId(), revision.getId());
    apply(profile.getUser().getId(), profile.getId(), command.profile());
    OffsetDateTime now = OffsetDateTime.now(clock);
    auditLogs.save(
        AdminAuditLog.record(
            admin,
            UPDATED_ACTION,
            AuditTargetType.PERSONAL_PROFILE,
            profile.getId(),
            command.reason().trim(),
            null,
            previousState,
            state(profile.getUser().getId(), revision.getId()),
            now));
    return new AdminPersonalResult(profile.getUser().getId(), profile.getId(), revision.getId());
  }

  private void apply(java.util.UUID userId, java.util.UUID profileId, AdminPersonalInput input) {
    updateProfileDraftService.update(
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
    updateProfileModalitiesService.update(
        new UpdateProfileModalitiesCommand(userId, profileId, input.modalityIds()));
    updateProfileServiceModesService.update(
        new UpdateProfileServiceModesCommand(userId, profileId, input.serviceModes()));
    updateProfileServiceAreasService.update(
        new UpdateProfileServiceAreasCommand(userId, profileId, input.serviceAreas()));
    if (input.crefRegistrationCode() != null) {
      upsertCrefService.upsert(
          new UpsertCrefCommand(
              userId, profileId, input.crefRegistrationCode(), input.crefDocumentImageKey()));
    }
  }

  private com.fasterxml.jackson.databind.JsonNode state(
      java.util.UUID userId, java.util.UUID revisionId) {
    return JsonNodeFactory.instance
        .objectNode()
        .put("origin", "ADMIN")
        .put("userId", userId.toString())
        .put("revisionId", revisionId.toString());
  }
}
