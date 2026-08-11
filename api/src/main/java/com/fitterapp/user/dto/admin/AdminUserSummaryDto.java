package com.fitterapp.user.dto.admin;

import com.fitterapp.user.entity.UserStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminUserSummaryDto(
    UUID userId,
    String fullName,
    String email,
    String phoneNumber,
    UserStatus status,
    List<AdminUserRoleDto> roles,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
