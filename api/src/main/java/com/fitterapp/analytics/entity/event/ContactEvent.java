package com.fitterapp.analytics.entity.event;

import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "contact_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContactEvent {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "personal_profile_id")
  private Profile personalProfile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "academy_profile_id")
  private AcademyProfile academyProfile;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EventSource source;

  @Column(length = 100)
  private String city;

  @Column(name = "unique_event", nullable = false)
  private boolean uniqueEvent;

  @Column(name = "idempotency_key_hash", length = 64)
  private String idempotencyKeyHash;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private OffsetDateTime occurredAt;

  public static ContactEvent whatsappToPersonal(
      User user, Profile personal, EventSource source, String city, OffsetDateTime occurredAt) {
    return whatsappToPersonal(user, personal, source, city, occurredAt, true, null);
  }

  public static ContactEvent whatsappToPersonal(
      User user,
      Profile personal,
      EventSource source,
      String city,
      OffsetDateTime occurredAt,
      boolean uniqueEvent,
      String idempotencyKeyHash) {
    ContactEvent event = base(user, source, city, occurredAt, uniqueEvent, idempotencyKeyHash);
    event.personalProfile = personal;
    return event;
  }

  public static ContactEvent whatsappToAcademy(
      User user,
      AcademyProfile academy,
      EventSource source,
      String city,
      OffsetDateTime occurredAt) {
    ContactEvent event = base(user, source, city, occurredAt, true, null);
    event.academyProfile = academy;
    return event;
  }

  private static ContactEvent base(
      User user,
      EventSource source,
      String city,
      OffsetDateTime occurredAt,
      boolean uniqueEvent,
      String idempotencyKeyHash) {
    ContactEvent event = new ContactEvent();
    event.user = user;
    event.source = source;
    event.city = city;
    event.occurredAt = occurredAt;
    event.uniqueEvent = uniqueEvent;
    event.idempotencyKeyHash = idempotencyKeyHash;
    return event;
  }
}
