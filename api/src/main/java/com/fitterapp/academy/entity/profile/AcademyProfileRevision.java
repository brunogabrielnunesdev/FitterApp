package com.fitterapp.academy.entity.profile;

import com.fitterapp.academy.entity.cnpj.AcademyCnpj;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "academy_profile_revisions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyProfileRevision {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "academy_id", nullable = false)
  private AcademyProfile academy;

  @Column(name = "version_number", nullable = false)
  private Integer versionNumber;

  @Column(length = 160)
  private String name;

  @Column(length = 2000)
  private String description;

  @Column(length = 20)
  private String whatsapp;

  @Column(length = 100)
  private String instagram;

  @Column(name = "logo_image_key", length = 255)
  private String logoImageKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cnpj_id")
  private AcademyCnpj cnpj;

  @Column(name = "postal_code", columnDefinition = "char(8)")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String postalCode;

  @Column(length = 160)
  private String street;

  @Column(name = "street_number", length = 20)
  private String streetNumber;

  @Column(name = "address_complement", length = 100)
  private String addressComplement;

  @Column(length = 100)
  private String neighborhood;

  @Column(length = 100)
  private String city;

  @Column(name = "state_code", columnDefinition = "char(2)")
  @JdbcTypeCode(SqlTypes.CHAR)
  private String stateCode;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private AcademyProfileRevisionStatus status;

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

  public static AcademyProfileRevision draft(
      AcademyProfile academy,
      int versionNumber,
      User createdBy,
      boolean requiresReview,
      OffsetDateTime createdAt) {
    AcademyProfileRevision revision = new AcademyProfileRevision();
    revision.academy = academy;
    revision.versionNumber = versionNumber;
    revision.createdBy = createdBy;
    revision.requiresReview = requiresReview;
    revision.status = AcademyProfileRevisionStatus.DRAFT;
    revision.createdAt = createdAt;
    revision.updatedAt = createdAt;
    return revision;
  }

  public void updatePublicData(
      String name,
      String description,
      String whatsapp,
      String instagram,
      String logoImageKey,
      OffsetDateTime updatedAt) {
    this.name = name;
    this.description = description;
    this.whatsapp = whatsapp;
    this.instagram = instagram;
    this.logoImageKey = logoImageKey;
    this.updatedAt = updatedAt;
  }

  public void updateAddress(
      String postalCode,
      String street,
      String streetNumber,
      String addressComplement,
      String neighborhood,
      String city,
      String stateCode,
      OffsetDateTime updatedAt) {
    this.postalCode = postalCode;
    this.street = street;
    this.streetNumber = streetNumber;
    this.addressComplement = addressComplement;
    this.neighborhood = neighborhood;
    this.city = city;
    this.stateCode = stateCode;
    this.updatedAt = updatedAt;
  }

  public void assignCnpj(AcademyCnpj cnpj, OffsetDateTime updatedAt) {
    this.cnpj = cnpj;
    this.updatedAt = updatedAt;
  }

  public void submit(OffsetDateTime submittedAt) {
    status = AcademyProfileRevisionStatus.PENDING_REVIEW;
    this.submittedAt = submittedAt;
    updatedAt = submittedAt;
  }

  public void approve(User reviewedBy, OffsetDateTime reviewedAt) {
    status = AcademyProfileRevisionStatus.APPROVED;
    this.reviewedBy = reviewedBy;
    this.reviewedAt = reviewedAt;
    rejectionReason = null;
    updatedAt = reviewedAt;
  }

  public void reject(User reviewedBy, String rejectionReason, OffsetDateTime reviewedAt) {
    status = AcademyProfileRevisionStatus.REJECTED;
    this.reviewedBy = reviewedBy;
    this.reviewedAt = reviewedAt;
    this.rejectionReason = rejectionReason;
    updatedAt = reviewedAt;
  }
}
