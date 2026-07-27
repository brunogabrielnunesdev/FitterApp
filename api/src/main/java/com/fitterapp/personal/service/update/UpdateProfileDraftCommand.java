package com.fitterapp.personal.service.update;

import java.util.UUID;

import com.fitterapp.personal.entity.service.PriceUnit;

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
        PriceUnit priceUnit) {
}
