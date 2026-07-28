package com.fitterapp.personal.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record UpsertCrefRequestDto(
    @NotBlank String registrationCode, @NotBlank String documentImageKey) {}
