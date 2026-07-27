package com.fitterapp.personal.service.service;

import java.util.List;
import java.util.UUID;

public record UpdateProfileServiceAreasResult(
        UUID profileId,
        UUID revisionId,
        List<ServiceAreaInput> serviceAreas) {
}
