package com.fitterapp.personal.service.review;

import com.fitterapp.moderation.entity.suspension.*;
import com.fitterapp.moderation.repository.ProfileSuspensionRepository;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.repository.UserRepository;
import java.time.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminProfileLifecycleService {
  private final ProfileRepository profiles;
  private final ProfileSuspensionRepository suspensions;
  private final UserRepository users;
  private final Clock clock;

  @Transactional
  public Result suspend(UUID profileId, UUID adminId, String reason) {
    var p =
        profiles
            .findByIdForUpdate(profileId)
            .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Perfil nÃ£o encontrado"));
    if (p.getStatus() != ProfileStatus.APPROVED && p.getStatus() != ProfileStatus.PUBLISHED)
      throw error(HttpStatus.CONFLICT, "Perfil nÃ£o pode ser suspenso neste estado");
    var actor =
        users
            .findById(adminId)
            .orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "Administrador nÃ£o encontrado"));
    var now = OffsetDateTime.now(clock);
    var suspension =
        ProfileSuspension.suspendPersonal(
            p, null, actor, reason.trim(), p.getStatus().name(), now, now.plusSeconds(1));
    p.suspend(now);
    suspensions.saveAndFlush(suspension);
    return new Result(p.getId(), suspension.getId(), p.getStatus(), now);
  }

  @Transactional
  public Result reactivate(UUID profileId, UUID adminId, String reason) {
    var p =
        profiles
            .findByIdForUpdate(profileId)
            .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Perfil nÃ£o encontrado"));
    if (p.getStatus() != ProfileStatus.SUSPENDED)
      throw error(HttpStatus.CONFLICT, "Perfil nÃ£o estÃ¡ suspenso");
    var suspension =
        suspensions
            .findFirstByPersonalProfileIdAndStatusOrderBySuspendedAtDesc(
                profileId, SuspensionStatus.ACTIVE)
            .orElseThrow(() -> error(HttpStatus.CONFLICT, "SuspensÃ£o ativa nÃ£o encontrada"));
    var actor =
        users
            .findById(adminId)
            .orElseThrow(() -> error(HttpStatus.UNAUTHORIZED, "Administrador nÃ£o encontrado"));
    var now = OffsetDateTime.now(clock);
    if (now.isBefore(suspension.getEligibleForReactivationAt()))
      throw error(HttpStatus.CONFLICT, "Perfil ainda nÃ£o pode ser reativado");
    ProfileStatus previous;
    try {
      previous = ProfileStatus.valueOf(suspension.getPreviousStatus());
    } catch (IllegalArgumentException e) {
      throw error(HttpStatus.CONFLICT, "Estado anterior invÃ¡lido");
    }
    if (previous != ProfileStatus.APPROVED && previous != ProfileStatus.PUBLISHED)
      throw error(HttpStatus.CONFLICT, "Estado anterior invÃ¡lido");
    if (previous == ProfileStatus.PUBLISHED && p.getPublishedRevision() == null)
      throw error(HttpStatus.CONFLICT, "VersÃ£o publicada nÃ£o encontrada");
    p.reactivate(previous, now);
    suspension.lift(actor, reason.trim(), now);
    return new Result(p.getId(), suspension.getId(), p.getStatus(), now);
  }

  private ResponseStatusException error(HttpStatus status, String message) {
    return new ResponseStatusException(status, message);
  }

  public record Result(
      UUID profileId, UUID suspensionId, ProfileStatus profileStatus, OffsetDateTime actionAt) {}
}
