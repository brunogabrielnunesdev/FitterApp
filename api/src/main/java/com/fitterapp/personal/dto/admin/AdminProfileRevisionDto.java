package com.fitterapp.personal.dto.admin;

import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.service.PriceUnit;
import com.fitterapp.personal.entity.service.ServiceMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminProfileRevisionDto(
    UUID revisionId,
    int versionNumber,
    ProfileRevisionStatus status,
    boolean requiresReview,
    String rejectionReason,
    String fullName,
    String biography,
    String whatsapp,
    String profileImageKey,
    Short experienceStartedYear,
    String certifications,
    String gymsDescription,
    Integer startingPriceCents,
    PriceUnit priceUnit,
    AdminCrefDto cref,
    List<AdminModalityDto> modalities,
    List<ServiceMode> serviceModes,
    List<AdminServiceAreaDto> serviceAreas,
    OffsetDateTime submittedAt,
    OffsetDateTime reviewedAt,
    UUID reviewedByUserId,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
