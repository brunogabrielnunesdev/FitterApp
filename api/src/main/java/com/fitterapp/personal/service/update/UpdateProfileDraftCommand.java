package com.fitterapp.personal.service.update;

import com.fitterapp.personal.entity.service.PriceUnit;
import java.util.UUID;

public record UpdateProfileDraftCommand(
    UUID userId,
    UUID profileId,
    String fullName,
    String biography,
    String whatsapp,
    Short experienceStartedYear,
    String certifications,
    String gymsDescription,
    Integer startingPriceCents,
    PriceUnit priceUnit) {}
