package com.fitterapp.personal.service.submission;

import java.util.UUID;

public record SubmitProfileForReviewCommand(UUID userId, UUID profileId) {}
