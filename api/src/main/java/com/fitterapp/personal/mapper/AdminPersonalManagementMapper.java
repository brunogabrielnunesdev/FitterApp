package com.fitterapp.personal.mapper;

import com.fitterapp.personal.dto.admin.AdminCreatePersonalRequestDto;
import com.fitterapp.personal.dto.admin.AdminPersonalProfileInputDto;
import com.fitterapp.personal.dto.admin.AdminUpdatePersonalRequestDto;
import com.fitterapp.personal.service.admin.AdminCreatePersonalCommand;
import com.fitterapp.personal.service.admin.AdminPersonalInput;
import com.fitterapp.personal.service.admin.AdminUpdatePersonalCommand;
import com.fitterapp.personal.service.service.ServiceAreaInput;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AdminPersonalManagementMapper {
  public AdminCreatePersonalCommand toCreateCommand(
      UUID adminUserId, AdminCreatePersonalRequestDto request) {
    return new AdminCreatePersonalCommand(
        adminUserId,
        request.accountFullName(),
        request.email(),
        request.phoneNumber(),
        request.temporaryPassword(),
        toInput(request.profile()),
        request.reason());
  }

  public AdminUpdatePersonalCommand toUpdateCommand(
      UUID adminUserId, UUID profileId, AdminUpdatePersonalRequestDto request) {
    return new AdminUpdatePersonalCommand(
        adminUserId, profileId, toInput(request.profile()), request.reason());
  }

  private AdminPersonalInput toInput(AdminPersonalProfileInputDto input) {
    var serviceAreas =
        input.serviceAreas().stream()
            .map(
                area ->
                    new ServiceAreaInput(
                        area.city(), area.stateCode(), area.neighborhood(), area.description()))
            .toList();
    return new AdminPersonalInput(
        input.fullName(),
        input.biography(),
        input.whatsapp(),
        input.experienceStartedYear(),
        input.certifications(),
        input.gymsDescription(),
        input.startingPriceCents(),
        input.priceUnit(),
        input.modalityIds(),
        input.serviceModes(),
        serviceAreas,
        input.cref() == null ? null : input.cref().registrationCode(),
        input.cref() == null ? null : input.cref().documentImageKey());
  }
}
