package com.fitterapp.personal.service.cref;

import java.util.UUID;

public record UpsertCrefCommand(
        UUID userId,
        UUID profileId,
        String registrationCode,
        String documentImageKey) {
}
