package com.fitterapp.personal.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUpdatePersonalRequestDto(
    @NotNull @Valid AdminPersonalProfileInputDto profile,
    @NotBlank @Size(max = 1500) String reason) {}
