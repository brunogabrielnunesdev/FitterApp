package com.fitterapp.personal.dto.admin;

import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminProfileSummaryDto(
    UUID profileId,
    UUID revisionId,
    String fullName,
    String email,
    ProfileStatus profileStatus,
    ProfileRevisionStatus revisionStatus,
    boolean published,
    OffsetDateTime submittedAt,
    OffsetDateTime updatedAt) {}
