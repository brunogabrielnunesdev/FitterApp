package com.fitterapp.personal.service.contact;

import com.fitterapp.analytics.entity.event.EventSource;
import java.util.UUID;

public record StartWhatsappContactCommand(String slug, UUID viewerId, EventSource source) {
  public StartWhatsappContactCommand(String slug, UUID viewerId) {
    this(slug, viewerId, EventSource.MOBILE_APP);
  }
}
