package com.fitterapp.academy.domain;

import com.fitterapp.academy.entity.cnpj.AcademyCnpj;
import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.academy.entity.profile.AcademyProfileRevision;
import com.fitterapp.academy.entity.profile.AcademyProfileRevisionStatus;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;

public final class AcademyProfileRevisionActions {
  private AcademyProfileRevisionActions() {}

  public static AcademyProfileRevision draft(
      AcademyProfile academy,
      int versionNumber,
      User createdBy,
      boolean requiresReview,
      OffsetDateTime createdAt) {
    AcademyProfileRevision revision = new AcademyProfileRevision();
    revision.setAcademy(academy);
    revision.setVersionNumber(versionNumber);
    revision.setCreatedBy(createdBy);
    revision.setRequiresReview(requiresReview);
    revision.setStatus(AcademyProfileRevisionStatus.DRAFT);
    revision.setCreatedAt(createdAt);
    revision.setUpdatedAt(createdAt);
    return revision;
  }

  public static void updatePublicData(
      AcademyProfileRevision entity,
      String name,
      String description,
      String whatsapp,
      String instagram,
      String logoImageKey,
      OffsetDateTime updatedAt) {
    entity.setName(name);
    entity.setDescription(description);
    entity.setWhatsapp(whatsapp);
    entity.setInstagram(instagram);
    entity.setLogoImageKey(logoImageKey);
    entity.setUpdatedAt(updatedAt);
  }

  public static void updateAddress(
      AcademyProfileRevision entity,
      String postalCode,
      String street,
      String streetNumber,
      String addressComplement,
      String neighborhood,
      String city,
      String stateCode,
      OffsetDateTime updatedAt) {
    entity.setPostalCode(postalCode);
    entity.setStreet(street);
    entity.setStreetNumber(streetNumber);
    entity.setAddressComplement(addressComplement);
    entity.setNeighborhood(neighborhood);
    entity.setCity(city);
    entity.setStateCode(stateCode);
    entity.setUpdatedAt(updatedAt);
  }

  public static void assignCnpj(
      AcademyProfileRevision entity, AcademyCnpj cnpj, OffsetDateTime updatedAt) {
    entity.setCnpj(cnpj);
    entity.setUpdatedAt(updatedAt);
  }

  public static void submit(AcademyProfileRevision entity, OffsetDateTime submittedAt) {
    entity.setStatus(AcademyProfileRevisionStatus.PENDING_REVIEW);
    entity.setSubmittedAt(submittedAt);
    entity.setUpdatedAt(submittedAt);
  }

  public static void approve(
      AcademyProfileRevision entity, User reviewedBy, OffsetDateTime reviewedAt) {
    entity.setStatus(AcademyProfileRevisionStatus.APPROVED);
    entity.setReviewedBy(reviewedBy);
    entity.setReviewedAt(reviewedAt);
    entity.setRejectionReason(null);
    entity.setUpdatedAt(reviewedAt);
  }

  public static void reject(
      AcademyProfileRevision entity,
      User reviewedBy,
      String rejectionReason,
      OffsetDateTime reviewedAt) {
    entity.setStatus(AcademyProfileRevisionStatus.REJECTED);
    entity.setReviewedBy(reviewedBy);
    entity.setReviewedAt(reviewedAt);
    entity.setRejectionReason(rejectionReason);
    entity.setUpdatedAt(reviewedAt);
  }
}
