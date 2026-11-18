package com.fitterapp.academy.entity.profile;

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
@Table(name = "academy_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademyProfile {

  @Id @UuidGenerator private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String handle;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AcademyProfileStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "current_revision_id")
  private AcademyProfileRevision currentRevision;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "published_revision_id")
  private AcademyProfileRevision publishedRevision;

  @Column(name = "published_at")
  private OffsetDateTime publishedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
