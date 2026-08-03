package com.fitterapp.personal.dto.publicprofile;

import com.fitterapp.personal.entity.service.PriceUnit;
import com.fitterapp.personal.entity.service.ServiceMode;
import java.util.List;
import java.util.UUID;

public record PublicProfileDetailDto(
    UUID profileId,
    String slug,
    String fullName,
    String biography,
    String profileImageKey,
    Short experienceStartedYear,
    String certifications,
    String gymsDescription,
    Integer startingPriceCents,
    PriceUnit priceUnit,
    List<PublicModalityDto> modalities,
    List<ServiceMode> serviceModes,
    List<PublicServiceAreaDto> serviceAreas) {}
