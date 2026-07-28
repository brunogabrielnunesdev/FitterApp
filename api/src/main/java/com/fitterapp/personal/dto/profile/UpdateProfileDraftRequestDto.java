package com.fitterapp.personal.dto.profile;

import com.fitterapp.personal.entity.service.PriceUnit;

public record UpdateProfileDraftRequestDto(
    String fullName,
    String biography,
    String whatsapp,
    Short experienceStartedYear,
    String certifications,
    String gymsDescription,
    Integer startingPriceCents,
    PriceUnit priceUnit) {}
