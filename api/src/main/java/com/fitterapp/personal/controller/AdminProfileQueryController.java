package com.fitterapp.personal.controller;

import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.service.query.AdminProfileQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/personal-profiles")
@RequiredArgsConstructor
public class AdminProfileQueryController {
  private final AdminProfileQueryService service;

  @GetMapping
  public AdminProfileQueryService.ProfilePage list(
      @RequestParam(required = false) ProfileStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.list(status, page, size);
  }

  @GetMapping("/{profileId}")
  public AdminProfileQueryService.Detail detail(@PathVariable UUID profileId) {
    return service.get(profileId);
  }
}
