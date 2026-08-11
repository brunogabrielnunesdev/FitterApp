package com.fitterapp.personal.controller;

import com.fitterapp.personal.dto.admin.AdminCreatePersonalRequestDto;
import com.fitterapp.personal.dto.admin.AdminPersonalActionResponseDto;
import com.fitterapp.personal.dto.admin.AdminUpdatePersonalRequestDto;
import com.fitterapp.personal.mapper.AdminPersonalManagementMapper;
import com.fitterapp.personal.service.admin.AdminPersonalManagementService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/personal-profiles")
@RequiredArgsConstructor
public class AdminPersonalManagementController {
  private final AdminPersonalManagementService service;
  private final AdminPersonalManagementMapper mapper;

  @PostMapping
  public ResponseEntity<AdminPersonalActionResponseDto> create(
      @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AdminCreatePersonalRequestDto request) {
    var result = service.create(mapper.toCreateCommand(adminUserId(jwt), request));
    var response =
        new AdminPersonalActionResponseDto(
            result.userId(), result.profileId(), result.revisionId());
    return ResponseEntity.created(
            URI.create("/api/v1/admin/personal-profiles/" + result.profileId()))
        .body(response);
  }

  @PutMapping("/{profileId}")
  public ResponseEntity<AdminPersonalActionResponseDto> update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID profileId,
      @Valid @RequestBody AdminUpdatePersonalRequestDto request) {
    var result = service.update(mapper.toUpdateCommand(adminUserId(jwt), profileId, request));
    return ResponseEntity.ok(
        new AdminPersonalActionResponseDto(
            result.userId(), result.profileId(), result.revisionId()));
  }

  private UUID adminUserId(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
