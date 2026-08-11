package com.fitterapp.personal.service.submission;

import com.fitterapp.analytics.entity.event.EventSource;
import java.util.UUID;

public record SubmitProfileForReviewCommand(UUID userId, UUID profileId, EventSource source) {
  public SubmitProfileForReviewCommand(UUID userId, UUID profileId) {
    this(userId, profileId, EventSource.MOBILE_APP);
  }
}
