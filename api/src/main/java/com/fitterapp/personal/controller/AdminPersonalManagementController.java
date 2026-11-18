package com.fitterapp.personal.controller;

import com.fitterapp.personal.entity.service.*;
import com.fitterapp.personal.service.management.AdminPersonalManagementService;
import com.fitterapp.personal.service.service.ServiceAreaInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/personal-profiles")
@RequiredArgsConstructor
public class AdminPersonalManagementController {
  private final AdminPersonalManagementService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AdminPersonalManagementService.Result create(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateRequest request) {
    return service.create(UUID.fromString(jwt.getSubject()), request);
  }

  @PutMapping("/{profileId}")
  public AdminPersonalManagementService.Result update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @Valid @RequestBody UpdateRequest request) {
    return service.update(UUID.fromString(jwt.getSubject()), profileId, request);
  }

  public record CreateRequest(
      @NotBlank @Size(max = 120) String accountFullName,
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(max = 20) String phoneNumber,
      @NotBlank @Size(min = 8, max = 128) String temporaryPassword,
      @NotNull @Valid ProfileInput profile,
      @NotBlank @Size(max = 1500) String reason) {}

  public record UpdateRequest(
      @NotNull @Valid ProfileInput profile, @NotBlank @Size(max = 1500) String reason) {}

  public record ProfileInput(
      @NotBlank @Size(max = 120) String fullName,
      @Size(max = 1500) String biography,
      @Size(max = 20) String whatsapp,
      Short experienceStartedYear,
      @Size(max = 1000) String certifications,
      @Size(max = 500) String gymsDescription,
      @PositiveOrZero Integer startingPriceCents,
      PriceUnit priceUnit,
      @NotNull List<@NotNull Short> modalityIds,
      @NotNull List<@NotNull ServiceMode> serviceModes,
      @NotNull List<@NotNull @Valid ServiceAreaInput> serviceAreas,
      @Valid CrefInput cref) {}

  public record CrefInput(
      @NotBlank @Size(max = 40) String registrationCode,
      @NotBlank @Size(max = 255) String documentImageKey) {}
}
