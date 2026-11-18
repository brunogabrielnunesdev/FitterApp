package com.fitterapp.personal.controller;

import com.fitterapp.personal.service.review.AdminProfileLifecycleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/personal-profiles")
@RequiredArgsConstructor
public class AdminProfileLifecycleController {
  private final AdminProfileLifecycleService service;

  @PatchMapping("/{profileId}/suspension")
  public AdminProfileLifecycleService.Result suspend(
      @PathVariable UUID profileId,
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody Reason request) {
    return service.suspend(profileId, UUID.fromString(jwt.getSubject()), request.reason());
  }

  @PatchMapping("/{profileId}/reactivation")
  public AdminProfileLifecycleService.Result reactivate(
      @PathVariable UUID profileId,
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody Reason request) {
    return service.reactivate(profileId, UUID.fromString(jwt.getSubject()), request.reason());
  }

  public record Reason(@NotBlank @Size(max = 1500) String reason) {}
}
