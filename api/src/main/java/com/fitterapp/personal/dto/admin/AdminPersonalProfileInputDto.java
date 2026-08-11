package com.fitterapp.personal.dto.admin;

import com.fitterapp.personal.entity.service.PriceUnit;
import com.fitterapp.personal.entity.service.ServiceMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AdminPersonalProfileInputDto(
    @NotBlank @Size(max = 120) String fullName,
    @Size(max = 1500) String biography,
    @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$") String whatsapp,
    Short experienceStartedYear,
    @Size(max = 1000) String certifications,
    @Size(max = 500) String gymsDescription,
    @PositiveOrZero Integer startingPriceCents,
    PriceUnit priceUnit,
    @NotNull List<Short> modalityIds,
    @NotNull List<ServiceMode> serviceModes,
    @NotNull List<@Valid AdminServiceAreaInputDto> serviceAreas,
    @Valid AdminCrefInputDto cref) {}
