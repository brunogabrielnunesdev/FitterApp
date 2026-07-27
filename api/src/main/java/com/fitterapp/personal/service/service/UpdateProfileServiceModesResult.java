package com.fitterapp.personal.service.service;

import java.util.List;
import java.util.UUID;

import com.fitterapp.personal.entity.service.ServiceMode;

public record UpdateProfileServiceModesResult(
        UUID profileId,
        UUID revisionId,
        List<ServiceMode> serviceModes) {
}
