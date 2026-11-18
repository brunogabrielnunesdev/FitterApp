package com.fitterapp.analytics.domain;

import com.fitterapp.analytics.entity.event.AppAccessEvent;
import com.fitterapp.analytics.entity.event.AppAccessEventType;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;

public final class AppAccessEventActions {
  private AppAccessEventActions() {}

  public static AppAccessEvent opened(User user, EventSource source, OffsetDateTime occurredAt) {
    return record(user, AppAccessEventType.OPENED, source, occurredAt);
  }

  public static AppAccessEvent returned(User user, EventSource source, OffsetDateTime occurredAt) {
    return record(user, AppAccessEventType.RETURNED, source, occurredAt);
  }

  private static AppAccessEvent record(
      User user, AppAccessEventType eventType, EventSource source, OffsetDateTime occurredAt) {
    AppAccessEvent event = new AppAccessEvent();
    event.setUser(user);
    event.setEventType(eventType);
    event.setSource(source);
    event.setOccurredAt(occurredAt);
    return event;
  }
}
