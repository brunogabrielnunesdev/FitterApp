package com.fitterapp.personal.dto.profile;

import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import java.util.UUID;

public record ProfileStatusResponseDto(
    UUID profileId,
    String fullName,
    ProfileStatus profileStatus,
    ProfileRevisionStatus revisionStatus,
    String rejectionReason) {}
