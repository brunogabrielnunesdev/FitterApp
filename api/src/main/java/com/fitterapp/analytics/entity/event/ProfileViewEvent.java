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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "profile_view_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
