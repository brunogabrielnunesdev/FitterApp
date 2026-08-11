package com.fitterapp.moderation.service.suspension;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fitterapp.analytics.entity.audit.AdminAuditLog;
import com.fitterapp.analytics.entity.audit.AuditTargetType;
import com.fitterapp.analytics.repository.AdminAuditLogRepository;
import com.fitterapp.moderation.entity.suspension.ProfileSuspension;
import com.fitterapp.moderation.entity.suspension.SuspensionStatus;
import com.fitterapp.moderation.exception.ModerationReasonRequiredException;
import com.fitterapp.moderation.exception.ProfileModerationStateException;
import com.fitterapp.moderation.repository.ProfileSuspensionRepository;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileModerationService {
  private static final String SUSPENDED_ACTION = "PERSONAL_PROFILE_SUSPENDED";
  private static final String REACTIVATED_ACTION = "PERSONAL_PROFILE_REACTIVATED";

  private final ProfileRepository profiles;
  private final ProfileSuspensionRepository suspensions;
  private final UserRepository users;
  private final AdminAuditLogRepository auditLogs;
  private final Clock clock;

  @Transactional
  public ProfileModerationResult suspend(SuspendProfileCommand command) {
    String reason = requiredReason(command.reason());
    var profile = profiles.findById(command.profileId()).orElseThrow(ProfileNotFoundException::new);
    ProfileStatus previousStatus = profile.getStatus();
    if (previousStatus != ProfileStatus.APPROVED && previousStatus != ProfileStatus.PUBLISHED) {
      throw new ProfileModerationStateException(
          "Only approved or published profiles can be suspended");
    }
    var admin = users.findById(command.adminUserId()).orElseThrow();
    OffsetDateTime now = OffsetDateTime.now(clock);
    var suspension =
        ProfileSuspension.suspendPersonal(
            profile, null, admin, reason, previousStatus.name(), now, now.plusSeconds(1));
    suspensions.save(suspension);
    profile.suspend(now);
    auditLogs.save(
        AdminAuditLog.record(
            admin,
            SUSPENDED_ACTION,
            AuditTargetType.PERSONAL_PROFILE,
            profile.getId(),
            reason,
            null,
            state(previousStatus),
            state(ProfileStatus.SUSPENDED),
            now));
    return new ProfileModerationResult(
        profile.getId(), suspension.getId(), profile.getStatus(), now);
  }

  @Transactional
  public ProfileModerationResult reactivate(ReactivateProfileCommand command) {
    String reason = requiredReason(command.reason());
    var profile = profiles.findById(command.profileId()).orElseThrow(ProfileNotFoundException::new);
    if (profile.getStatus() != ProfileStatus.SUSPENDED) {
      throw new ProfileModerationStateException("Profile is not suspended");
    }
    var suspension =
        suspensions
            .findByPersonalProfileIdAndStatus(profile.getId(), SuspensionStatus.ACTIVE)
            .orElseThrow(
                () -> new ProfileModerationStateException("Profile has no active suspension"));
    var admin = users.findById(command.adminUserId()).orElseThrow();
    OffsetDateTime now = OffsetDateTime.now(clock);
    ProfileStatus restoredStatus = ProfileStatus.valueOf(suspension.getPreviousStatus());
    suspension.lift(admin, reason, now);
    profile.reactivate(restoredStatus, now);
    auditLogs.save(
        AdminAuditLog.record(
            admin,
            REACTIVATED_ACTION,
            AuditTargetType.PERSONAL_PROFILE,
            profile.getId(),
            reason,
            null,
            state(ProfileStatus.SUSPENDED),
            state(restoredStatus),
            now));
    return new ProfileModerationResult(
        profile.getId(), suspension.getId(), profile.getStatus(), now);
  }

  private String requiredReason(String reason) {
    if (reason == null || reason.isBlank()) throw new ModerationReasonRequiredException();
    return reason.trim();
  }

  private com.fasterxml.jackson.databind.JsonNode state(ProfileStatus status) {
    return JsonNodeFactory.instance.objectNode().put("status", status.name());
  }
}
