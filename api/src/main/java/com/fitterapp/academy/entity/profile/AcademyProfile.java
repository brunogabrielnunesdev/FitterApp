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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "academy_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  public static AcademyProfile draft(String handle, OffsetDateTime createdAt) {
    AcademyProfile profile = new AcademyProfile();
    profile.handle = handle;
    profile.status = AcademyProfileStatus.DRAFT;
    profile.createdAt = createdAt;
    profile.updatedAt = createdAt;
    return profile;
  }

  public void setCurrentRevision(AcademyProfileRevision revision, OffsetDateTime updatedAt) {
    currentRevision = revision;
    this.updatedAt = updatedAt;
  }

  public void submitForReview(OffsetDateTime submittedAt) {
    status = AcademyProfileStatus.PENDING_REVIEW;
    updatedAt = submittedAt;
  }

  public void approve(OffsetDateTime approvedAt) {
    status = AcademyProfileStatus.APPROVED;
    updatedAt = approvedAt;
  }

  public void reject(OffsetDateTime rejectedAt) {
    status = AcademyProfileStatus.REJECTED;
    updatedAt = rejectedAt;
  }

  public void publish(AcademyProfileRevision revision, OffsetDateTime publishedAt) {
    currentRevision = revision;
    publishedRevision = revision;
    status = AcademyProfileStatus.PUBLISHED;
    this.publishedAt = publishedAt;
    updatedAt = publishedAt;
  }

  public void unpublish(OffsetDateTime unpublishedAt) {
    status = AcademyProfileStatus.APPROVED;
    publishedRevision = null;
    publishedAt = null;
    updatedAt = unpublishedAt;
  }

  public void suspend(OffsetDateTime suspendedAt) {
    status = AcademyProfileStatus.SUSPENDED;
    updatedAt = suspendedAt;
  }
}
