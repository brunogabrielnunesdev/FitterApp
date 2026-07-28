package com.fitterapp.personal.service.service;

import com.fitterapp.personal.entity.service.ServiceMode;
import java.util.List;
import java.util.UUID;

public record UpdateProfileServiceModesResult(
    UUID profileId, UUID revisionId, List<ServiceMode> serviceModes) {}
