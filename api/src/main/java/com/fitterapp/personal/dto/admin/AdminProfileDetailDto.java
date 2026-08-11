package com.fitterapp.personal.dto.admin;

import com.fitterapp.personal.entity.profile.ProfileStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminProfileDetailDto(
    UUID profileId,
    String slug,
    ProfileStatus status,
    boolean published,
    UUID publishedRevisionId,
    OffsetDateTime publishedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    AdminAccountDto account,
    AdminProfileRevisionDto revision) {}
