package com.fitterapp.analytics.domain;

import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.analytics.entity.event.ContactEvent;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;

public final class ContactEventActions {
  private ContactEventActions() {}

  public static ContactEvent whatsappToPersonal(
      User user, Profile personal, EventSource source, String city, OffsetDateTime occurredAt) {
    ContactEvent event = base(user, source, city, occurredAt);
    event.setPersonalProfile(personal);
    return event;
  }

  public static ContactEvent whatsappToAcademy(
      User user,
      AcademyProfile academy,
      EventSource source,
      String city,
      OffsetDateTime occurredAt) {
    ContactEvent event = base(user, source, city, occurredAt);
    event.setAcademyProfile(academy);
    return event;
  }

  private static ContactEvent base(
      User user, EventSource source, String city, OffsetDateTime occurredAt) {
    ContactEvent event = new ContactEvent();
    event.setUser(user);
    event.setSource(source);
    event.setCity(city);
    event.setOccurredAt(occurredAt);
    return event;
  }
}
