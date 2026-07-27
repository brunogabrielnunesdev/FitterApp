package com.fitterapp.personal.service.service;

import java.util.List;
import java.util.UUID;

import com.fitterapp.personal.entity.service.ServiceMode;

public record UpdateProfileServiceModesCommand(
        UUID userId,
        UUID profileId,
        List<ServiceMode> serviceModes) {
}
