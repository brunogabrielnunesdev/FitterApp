package com.fitterapp.personal.service.service;

import com.fitterapp.personal.entity.service.ServiceMode;
import java.util.List;
import java.util.UUID;

public record UpdateProfileServiceModesCommand(
    UUID userId, UUID profileId, List<ServiceMode> serviceModes) {}
