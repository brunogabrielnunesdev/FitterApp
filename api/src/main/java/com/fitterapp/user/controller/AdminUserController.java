package com.fitterapp.user.controller;

import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.service.AdminUserQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
  private final AdminUserQueryService service;

  @GetMapping
  public AdminUserQueryService.UserPage list(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) UserStatus status,
      @RequestParam(required = false) RoleName role,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.list(query, status, role, page, size);
  }

  @GetMapping("/{userId}")
  public AdminUserQueryService.UserView get(@PathVariable UUID userId) {
    return service.get(userId);
  }
}
