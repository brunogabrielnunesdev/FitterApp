package com.fitterapp.personal.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCrefInputDto(
    @NotBlank @Size(max = 40) String registrationCode,
    @NotBlank @Size(max = 255) String documentImageKey) {}
