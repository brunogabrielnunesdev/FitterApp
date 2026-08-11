package com.fitterapp.user.dto.admin;

import com.fitterapp.user.entity.RoleName;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserRoleDto(
    RoleName name, OffsetDateTime grantedAt, UUID grantedByUserId) {}
