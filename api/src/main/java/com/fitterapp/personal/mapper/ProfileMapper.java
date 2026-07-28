package com.fitterapp.personal.mapper;

import com.fitterapp.personal.dto.profile.CreateProfileRequestDto;
import com.fitterapp.personal.dto.profile.ProfileStatusResponseDto;
import com.fitterapp.personal.dto.profile.ServiceAreaRequestDto;
import com.fitterapp.personal.dto.profile.UpdateModalitiesRequestDto;
import com.fitterapp.personal.dto.profile.UpdateProfileDraftRequestDto;
import com.fitterapp.personal.dto.profile.UpdateServiceAreasRequestDto;
import com.fitterapp.personal.dto.profile.UpdateServiceModesRequestDto;
import com.fitterapp.personal.dto.profile.UpsertCrefRequestDto;
import com.fitterapp.personal.service.create.CreateProfileCommand;
import com.fitterapp.personal.service.cref.UpsertCrefCommand;
import com.fitterapp.personal.service.modality.UpdateProfileModalitiesCommand;
import com.fitterapp.personal.service.service.ServiceAreaInput;
import com.fitterapp.personal.service.service.UpdateProfileServiceAreasCommand;
import com.fitterapp.personal.service.service.UpdateProfileServiceModesCommand;
import com.fitterapp.personal.service.update.UpdateProfileDraftCommand;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

  public CreateProfileCommand toCreateCommand(UUID userId, CreateProfileRequestDto request) {
    return new CreateProfileCommand(userId);
  }

  public UpdateProfileDraftCommand toUpdateCommand(
      UUID userId, UUID profileId, UpdateProfileDraftRequestDto request) {
    return new UpdateProfileDraftCommand(
        userId,
        profileId,
        request.fullName(),
        request.biography(),
        request.whatsapp(),
        request.experienceStartedYear(),
        request.certifications(),
        request.gymsDescription(),
        request.startingPriceCents(),
        request.priceUnit());
  }

  public UpsertCrefCommand toCrefCommand(
      UUID userId, UUID profileId, UpsertCrefRequestDto request) {
    return new UpsertCrefCommand(
        userId, profileId, request.registrationCode(), request.documentImageKey());
  }

  public UpdateProfileModalitiesCommand toModalitiesCommand(
      UUID userId, UUID profileId, UpdateModalitiesRequestDto request) {
    return new UpdateProfileModalitiesCommand(userId, profileId, request.modalityIds());
  }

  public UpdateProfileServiceModesCommand toServiceModesCommand(
      UUID userId, UUID profileId, UpdateServiceModesRequestDto request) {
    return new UpdateProfileServiceModesCommand(userId, profileId, request.serviceModes());
  }

  public UpdateProfileServiceAreasCommand toServiceAreasCommand(
      UUID userId, UUID profileId, UpdateServiceAreasRequestDto request) {
    List<ServiceAreaInput> serviceAreas =
        request.serviceAreas().stream().map(this::toServiceAreaInput).toList();
    return new UpdateProfileServiceAreasCommand(userId, profileId, serviceAreas);
  }

  private ServiceAreaInput toServiceAreaInput(ServiceAreaRequestDto request) {
    return new ServiceAreaInput(
        request.city(), request.stateCode(), request.neighborhood(), request.description());
  }

  public ProfileStatusResponseDto toStatusResponse(
      com.fitterapp.personal.entity.profile.Profile profile) {
    var revision = profile.getCurrentRevision();
    return new ProfileStatusResponseDto(
        profile.getId(),
        profile.getFullName(),
        profile.getStatus(),
        revision == null ? null : revision.getStatus(),
        revision == null ? null : revision.getRejectionReason());
  }
}
