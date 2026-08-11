package com.fitterapp.personal.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminServiceAreaInputDto(
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String stateCode,
    @Size(max = 100) String neighborhood,
    @Size(max = 255) String description) {}
