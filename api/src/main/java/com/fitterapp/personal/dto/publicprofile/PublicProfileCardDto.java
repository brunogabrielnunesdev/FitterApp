package com.fitterapp.personal.dto.publicprofile;

import com.fitterapp.personal.entity.service.PriceUnit;
import java.util.UUID;

public record PublicProfileCardDto(
    UUID profileId,
    String slug,
    String fullName,
    String biography,
    String profileImageKey,
    Integer startingPriceCents,
    PriceUnit priceUnit) {}
