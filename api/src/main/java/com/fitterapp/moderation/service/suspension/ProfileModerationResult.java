package com.fitterapp.moderation.service.suspension;

import com.fitterapp.personal.entity.profile.ProfileStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileModerationResult(
    UUID profileId, UUID suspensionId, ProfileStatus profileStatus, OffsetDateTime actionAt) {}
