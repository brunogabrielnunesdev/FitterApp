package com.fitterapp.analytics.entity.event;

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
@Table(name = "funnel_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FunnelEvent {
  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "personal_profile_id")
  private Profile personalProfile;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 30)
  private FunnelEventType eventType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EventSource source;

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private OffsetDateTime occurredAt;

  public static FunnelEvent accountCompleted(
      User user, EventSource source, OffsetDateTime occurredAt) {
    return record(user, null, FunnelEventType.ACCOUNT_COMPLETED, source, occurredAt);
  }

  public static FunnelEvent profileStarted(
      User user, Profile profile, EventSource source, OffsetDateTime occurredAt) {
    return record(user, profile, FunnelEventType.PROFILE_STARTED, source, occurredAt);
  }

  public static FunnelEvent profileSubmitted(
      User user, Profile profile, EventSource source, OffsetDateTime occurredAt) {
    return record(user, profile, FunnelEventType.PROFILE_SUBMITTED, source, occurredAt);
  }

  private static FunnelEvent record(
      User user,
      Profile profile,
      FunnelEventType eventType,
      EventSource source,
      OffsetDateTime occurredAt) {
    FunnelEvent event = new FunnelEvent();
    event.user = user;
    event.personalProfile = profile;
    event.eventType = eventType;
    event.source = source;
    event.occurredAt = occurredAt;
    return event;
  }
}
