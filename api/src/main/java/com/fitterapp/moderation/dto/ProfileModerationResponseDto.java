package com.fitterapp.moderation.dto;

import com.fitterapp.personal.entity.profile.ProfileStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileModerationResponseDto(
    UUID profileId, UUID suspensionId, ProfileStatus profileStatus, OffsetDateTime actionAt) {}
