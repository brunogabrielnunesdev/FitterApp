package com.fitterapp.academy.domain;

import com.fitterapp.academy.entity.cnpj.AcademyCnpj;
import com.fitterapp.academy.entity.cnpj.AcademyCnpjStatus;
import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;

public final class AcademyCnpjActions {
  private AcademyCnpjActions() {}

  public static AcademyCnpj pendingReview(
      AcademyProfile academy, String registrationNumber, OffsetDateTime createdAt) {
    AcademyCnpj cnpj = new AcademyCnpj();
    cnpj.setAcademy(academy);
    cnpj.setRegistrationNumber(registrationNumber);
    cnpj.setStatus(AcademyCnpjStatus.PENDING_REVIEW);
    cnpj.setCreatedAt(createdAt);
    cnpj.setUpdatedAt(createdAt);
    return cnpj;
  }

  public static void verify(AcademyCnpj entity, User verifiedBy, OffsetDateTime verifiedAt) {
    entity.setStatus(AcademyCnpjStatus.VERIFIED);
    entity.setVerifiedBy(verifiedBy);
    entity.setVerifiedAt(verifiedAt);
    entity.setRejectionReason(null);
    entity.setUpdatedAt(verifiedAt);
  }

  public static void reject(
      AcademyCnpj entity, User verifiedBy, String rejectionReason, OffsetDateTime rejectedAt) {
    entity.setStatus(AcademyCnpjStatus.REJECTED);
    entity.setVerifiedBy(verifiedBy);
    entity.setVerifiedAt(rejectedAt);
    entity.setRejectionReason(rejectionReason);
    entity.setUpdatedAt(rejectedAt);
  }
}
