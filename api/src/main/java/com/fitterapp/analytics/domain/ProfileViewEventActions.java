package com.fitterapp.analytics.domain;

import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.entity.event.ProfileViewEvent;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;

public final class ProfileViewEventActions {
  private ProfileViewEventActions() {}

  public static ProfileViewEvent personalView(
      User viewer, Profile personal, EventSource source, String city, OffsetDateTime occurredAt) {
    ProfileViewEvent event = base(viewer, source, city, occurredAt);
    event.setPersonalProfile(personal);
    return event;
  }

  public static ProfileViewEvent academyView(
      User viewer,
      AcademyProfile academy,
      EventSource source,
      String city,
      OffsetDateTime occurredAt) {
    ProfileViewEvent event = base(viewer, source, city, occurredAt);
    event.setAcademyProfile(academy);
    return event;
  }

  private static ProfileViewEvent base(
      User viewer, EventSource source, String city, OffsetDateTime occurredAt) {
    ProfileViewEvent event = new ProfileViewEvent();
    event.setViewer(viewer);
    event.setSource(source);
    event.setCity(city);
    event.setOccurredAt(occurredAt);
    return event;
  }
}
