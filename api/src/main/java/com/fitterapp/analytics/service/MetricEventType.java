package com.fitterapp.analytics.service;

import java.time.Duration;

public enum MetricEventType {
  SEARCH(Duration.ofMinutes(5)),
  PROFILE_VIEW(Duration.ofMinutes(30)),
  WHATSAPP_CONTACT(Duration.ofMinutes(10));

  private final Duration deduplicationWindow;

  MetricEventType(Duration deduplicationWindow) {
    this.deduplicationWindow = deduplicationWindow;
  }

  public Duration deduplicationWindow() {
    return deduplicationWindow;
  }
}
