package com.fitterapp.personal.dto.modality;

import jakarta.validation.constraints.NotNull;

public record AdminModalityActivationRequestDto(@NotNull Boolean active) {}
