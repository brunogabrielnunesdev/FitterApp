package com.fitterapp.personal.service.create;

import com.fitterapp.analytics.entity.event.EventSource;
import java.util.UUID;

public record CreateProfileCommand(UUID userId, EventSource source) {
  public CreateProfileCommand(UUID userId) {
    this(userId, EventSource.MOBILE_APP);
  }
}
