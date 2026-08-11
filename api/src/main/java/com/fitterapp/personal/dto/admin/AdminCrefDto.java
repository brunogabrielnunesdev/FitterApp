package com.fitterapp.personal.dto.admin;

import com.fitterapp.personal.entity.cref.CrefStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminCrefDto(
    UUID id,
    String registrationCode,
    String documentImageKey,
    CrefStatus status,
    String rejectionReason,
    OffsetDateTime verifiedAt) {}
