package com.fitterapp.user.controller;

import com.fitterapp.user.dto.admin.AdminUserDetailDto;
import com.fitterapp.user.dto.admin.AdminUserPageDto;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.mapper.AdminUserMapper;
import com.fitterapp.user.service.query.GetAdminUserService;
import com.fitterapp.user.service.query.ListAdminUsersService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
public class AdminUserController {
  private final ListAdminUsersService listAdminUsersService;
  private final GetAdminUserService getAdminUserService;
  private final AdminUserMapper mapper;

  @GetMapping
  public ResponseEntity<AdminUserPageDto> list(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) UserStatus status,
      @RequestParam(required = false) RoleName role,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 100);
    var pageable =
        PageRequest.of(
            safePage, safeSize, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));
    return ResponseEntity.ok(
        mapper.toPage(listAdminUsersService.list(query, status, role, pageable)));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<AdminUserDetailDto> get(@PathVariable UUID userId) {
    return ResponseEntity.ok(mapper.toDetail(getAdminUserService.get(userId)));
  }
}
