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
@Table(name = "profile_view_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileViewEvent {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "viewer_user_id")
  private User viewer;

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

  @Column(name = "occurred_at", nullable = false, updatable = false)
  private OffsetDateTime occurredAt;

  public static ProfileViewEvent personalView(
      User viewer, Profile personal, EventSource source, String city, OffsetDateTime occurredAt) {
    ProfileViewEvent event = base(viewer, source, city, occurredAt);
    event.personalProfile = personal;
    return event;
  }

  public static ProfileViewEvent academyView(
      User viewer,
      AcademyProfile academy,
      EventSource source,
      String city,
      OffsetDateTime occurredAt) {
    ProfileViewEvent event = base(viewer, source, city, occurredAt);
    event.academyProfile = academy;
    return event;
  }

  private static ProfileViewEvent base(
      User viewer, EventSource source, String city, OffsetDateTime occurredAt) {
    ProfileViewEvent event = new ProfileViewEvent();
    event.viewer = viewer;
    event.source = source;
    event.city = city;
    event.occurredAt = occurredAt;
    return event;
  }
}
