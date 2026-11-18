package com.fitterapp.academy.domain;

import com.fitterapp.academy.entity.member.AcademyMember;
import com.fitterapp.academy.entity.member.AcademyMemberId;
import com.fitterapp.academy.entity.member.AcademyMemberRole;
import com.fitterapp.academy.entity.member.AcademyMemberStatus;
import com.fitterapp.academy.entity.profile.AcademyProfile;
import com.fitterapp.user.entity.User;
import java.time.OffsetDateTime;

public final class AcademyMemberActions {
  private AcademyMemberActions() {}

  public static AcademyMember active(
      AcademyProfile academy, User user, AcademyMemberRole role, OffsetDateTime joinedAt) {
    AcademyMember member = new AcademyMember();
    member.setId(new AcademyMemberId(academy.getId(), user.getId()));
    member.setAcademy(academy);
    member.setUser(user);
    member.setRole(role);
    member.setStatus(AcademyMemberStatus.ACTIVE);
    member.setJoinedAt(joinedAt);
    member.setCreatedAt(joinedAt);
    member.setUpdatedAt(joinedAt);
    return member;
  }

  public static void changeRole(
      AcademyMember entity, AcademyMemberRole role, OffsetDateTime updatedAt) {
    entity.setRole(role);
    entity.setUpdatedAt(updatedAt);
  }

  public static void deactivate(AcademyMember entity, OffsetDateTime deactivatedAt) {
    entity.setStatus(AcademyMemberStatus.INACTIVE);
    entity.setDeactivatedAt(deactivatedAt);
    entity.setUpdatedAt(deactivatedAt);
  }
}
