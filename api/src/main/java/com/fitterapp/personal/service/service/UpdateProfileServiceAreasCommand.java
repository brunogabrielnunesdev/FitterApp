package com.fitterapp.personal.service.service;

import java.util.List;
import java.util.UUID;

public record UpdateProfileServiceAreasCommand(
    UUID userId, UUID profileId, List<ServiceAreaInput> serviceAreas) {}
