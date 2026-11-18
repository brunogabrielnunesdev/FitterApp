package com.fitterapp.academy.domain;

import com.fitterapp.academy.entity.partnership.AcademyPersonalPartnership;
import com.fitterapp.academy.entity.partnership.PartnershipInitiator;
import com.fitterapp.academy.entity.partnership.PartnershipStatus;
import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;

public final class AcademyPersonalPartnershipActions {
  private AcademyPersonalPartnershipActions() {}

  public static AcademyPersonalPartnership request(
      AcademyProfile academy,
      Profile personal,
      PartnershipInitiator initiatedBy,
      User requestedBy,
      OffsetDateTime requestedAt) {
    AcademyPersonalPartnership partnership = new AcademyPersonalPartnership();
    partnership.setAcademy(academy);
    partnership.setPersonal(personal);
    partnership.setInitiatedBy(initiatedBy);
    partnership.setRequestedBy(requestedBy);
    partnership.setStatus(PartnershipStatus.PENDING);
    partnership.setRequestedAt(requestedAt);
    partnership.setCreatedAt(requestedAt);
    partnership.setUpdatedAt(requestedAt);
    return partnership;
  }

  public static void accept(
      AcademyPersonalPartnership entity, User respondedBy, OffsetDateTime respondedAt) {
    entity.setStatus(PartnershipStatus.ACCEPTED);
    entity.setRespondedBy(respondedBy);
    entity.setRespondedAt(respondedAt);
    entity.setUpdatedAt(respondedAt);
  }

  public static void reject(
      AcademyPersonalPartnership entity, User respondedBy, OffsetDateTime respondedAt) {
    entity.setStatus(PartnershipStatus.REJECTED);
    entity.setRespondedBy(respondedBy);
    entity.setRespondedAt(respondedAt);
    entity.setUpdatedAt(respondedAt);
  }

  public static void end(AcademyPersonalPartnership entity, OffsetDateTime endedAt) {
    entity.setStatus(PartnershipStatus.ENDED);
    entity.setEndedAt(endedAt);
    entity.setUpdatedAt(endedAt);
  }
}
