package com.fitterapp.academy.domain;

import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.academy.entity.profile.AcademyProfileRevision;
import com.fitterapp.academy.entity.profile.AcademyProfileStatus;
import java.time.OffsetDateTime;

public final class AcademyProfileActions {
  private AcademyProfileActions() {}

  public static AcademyProfile draft(String handle, OffsetDateTime createdAt) {
    AcademyProfile profile = new AcademyProfile();
    profile.setHandle(handle);
    profile.setStatus(AcademyProfileStatus.DRAFT);
    profile.setCreatedAt(createdAt);
    profile.setUpdatedAt(createdAt);
    return profile;
  }

  public static void setCurrentRevision(
      AcademyProfile entity, AcademyProfileRevision revision, OffsetDateTime updatedAt) {
    entity.setCurrentRevision(revision);
    entity.setUpdatedAt(updatedAt);
  }

  public static void submitForReview(AcademyProfile entity, OffsetDateTime submittedAt) {
    entity.setStatus(AcademyProfileStatus.PENDING_REVIEW);
    entity.setUpdatedAt(submittedAt);
  }

  public static void approve(AcademyProfile entity, OffsetDateTime approvedAt) {
    entity.setStatus(AcademyProfileStatus.APPROVED);
    entity.setUpdatedAt(approvedAt);
  }

  public static void reject(AcademyProfile entity, OffsetDateTime rejectedAt) {
    entity.setStatus(AcademyProfileStatus.REJECTED);
    entity.setUpdatedAt(rejectedAt);
  }

  public static void publish(
      AcademyProfile entity, AcademyProfileRevision revision, OffsetDateTime publishedAt) {
    entity.setCurrentRevision(revision);
    entity.setPublishedRevision(revision);
    entity.setStatus(AcademyProfileStatus.PUBLISHED);
    entity.setPublishedAt(publishedAt);
    entity.setUpdatedAt(publishedAt);
  }

  public static void unpublish(AcademyProfile entity, OffsetDateTime unpublishedAt) {
    entity.setStatus(AcademyProfileStatus.APPROVED);
    entity.setPublishedRevision(null);
    entity.setPublishedAt(null);
    entity.setUpdatedAt(unpublishedAt);
  }

  public static void suspend(AcademyProfile entity, OffsetDateTime suspendedAt) {
    entity.setStatus(AcademyProfileStatus.SUSPENDED);
    entity.setUpdatedAt(suspendedAt);
  }
}
