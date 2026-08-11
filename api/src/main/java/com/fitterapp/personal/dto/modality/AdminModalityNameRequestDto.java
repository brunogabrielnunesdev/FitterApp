package com.fitterapp.personal.dto.modality;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminModalityNameRequestDto(@NotBlank @Size(max = 80) String name) {}
