package com.fitterapp.personal.dto.profile;

import com.fitterapp.personal.entity.cref.CrefStatus;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.entity.service.PriceUnit;
import com.fitterapp.personal.entity.service.ServiceMode;
import java.util.List;
import java.util.UUID;

public record ProfileDraftResponseDto(
    UUID profileId,
    UUID revisionId,
    ProfileStatus profileStatus,
    ProfileRevisionStatus revisionStatus,
    String rejectionReason,
    String fullName,
    String biography,
    String whatsapp,
    Short experienceStartedYear,
    String certifications,
    String gymsDescription,
    Integer startingPriceCents,
    PriceUnit priceUnit,
    String crefRegistrationCode,
    String crefDocumentImageKey,
    CrefStatus crefStatus,
    List<Short> modalityIds,
    List<ServiceMode> serviceModes,
    List<ServiceAreaRequestDto> serviceAreas) {}
