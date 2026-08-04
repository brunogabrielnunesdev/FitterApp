package com.fitterapp.personal.entity.profile;

import com.fitterapp.personal.entity.cref.Cref;
import com.fitterapp.personal.entity.service.PriceUnit;
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
@Table(name = "personal_profile_revisions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileRevision {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "personal_id", nullable = false)
  private Profile personal;

  @Column(name = "version_number", nullable = false)
  private Integer versionNumber;

  @Column(name = "full_name", length = 120)
  private String fullName;

  @Column(length = 1500)
  private String biography;

  @Column(length = 20)
  private String whatsapp;

  @Column(name = "profile_image_key", length = 255)
  private String profileImageKey;

  @Column(name = "experience_started_year")
  private Short experienceStartedYear;

  @Column(length = 1000)
  private String certifications;

  @Column(name = "gyms_description", length = 500)
  private String gymsDescription;

  @Column(name = "starting_price_cents")
  private Integer startingPriceCents;

  @Enumerated(EnumType.STRING)
  @Column(name = "price_unit", length = 30)
  private PriceUnit priceUnit;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cref_id")
  private Cref cref;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ProfileRevisionStatus status;

  @Column(name = "requires_review", nullable = false)
  private boolean requiresReview;

  @Column(name = "rejection_reason", length = 500)
  private String rejectionReason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  @Column(name = "submitted_at")
  private OffsetDateTime submittedAt;

  @Column(name = "reviewed_at")
  private OffsetDateTime reviewedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by")
  private User reviewedBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public static ProfileRevision draft(
      Profile personal,
      int versionNumber,
      User createdBy,
      boolean requiresReview,
      OffsetDateTime createdAt) {
    ProfileRevision revision = new ProfileRevision();
    revision.personal = personal;
    revision.versionNumber = versionNumber;
    revision.createdBy = createdBy;
    revision.requiresReview = requiresReview;
    revision.status = ProfileRevisionStatus.DRAFT;
    revision.createdAt = createdAt;
    revision.updatedAt = createdAt;
    return revision;
  }

  public static ProfileRevision draftFrom(
      Profile personal,
      int versionNumber,
      User createdBy,
      ProfileRevision source,
      OffsetDateTime createdAt) {
    ProfileRevision revision = draft(personal, versionNumber, createdBy, true, createdAt);
    revision.fullName = source.fullName;
    revision.biography = source.biography;
    revision.whatsapp = source.whatsapp;
    revision.profileImageKey = source.profileImageKey;
    revision.experienceStartedYear = source.experienceStartedYear;
    revision.certifications = source.certifications;
    revision.gymsDescription = source.gymsDescription;
    revision.startingPriceCents = source.startingPriceCents;
    revision.priceUnit = source.priceUnit;
    revision.cref = source.cref;
    return revision;
  }

  public void updateProfessionalData(
      String fullName,
      String biography,
      String whatsapp,
      Short experienceStartedYear,
      String certifications,
      String gymsDescription,
      OffsetDateTime updatedAt) {
    this.fullName = fullName;
    this.biography = biography;
    this.whatsapp = whatsapp;
    this.experienceStartedYear = experienceStartedYear;
    this.certifications = certifications;
    this.gymsDescription = gymsDescription;
    this.updatedAt = updatedAt;
  }

  public void updateProfileImage(String profileImageKey, OffsetDateTime updatedAt) {
    this.profileImageKey = profileImageKey;
    this.updatedAt = updatedAt;
  }

  public void updateStartingPrice(
      Integer startingPriceCents, PriceUnit priceUnit, OffsetDateTime updatedAt) {
    this.startingPriceCents = startingPriceCents;
    this.priceUnit = priceUnit;
    this.updatedAt = updatedAt;
  }

  public void assignCref(Cref cref, OffsetDateTime updatedAt) {
    this.cref = cref;
    this.updatedAt = updatedAt;
  }

  public void submit(OffsetDateTime submittedAt) {
    status = ProfileRevisionStatus.PENDING_REVIEW;
    this.submittedAt = submittedAt;
    this.updatedAt = submittedAt;
  }

  public void approve(User reviewedBy, OffsetDateTime reviewedAt) {
    status = ProfileRevisionStatus.APPROVED;
    this.reviewedBy = reviewedBy;
    this.reviewedAt = reviewedAt;
    this.rejectionReason = null;
    this.updatedAt = reviewedAt;
  }

  public void reject(User reviewedBy, String rejectionReason, OffsetDateTime reviewedAt) {
    status = ProfileRevisionStatus.REJECTED;
    this.reviewedBy = reviewedBy;
    this.reviewedAt = reviewedAt;
    this.rejectionReason = rejectionReason;
    this.updatedAt = reviewedAt;
  }
}
