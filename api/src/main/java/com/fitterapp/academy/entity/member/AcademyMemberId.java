package com.fitterapp.academy.entity.member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademyMemberId implements Serializable {

  @Column(name = "academy_id")
  @NonNull
  private UUID academyId;

  @Column(name = "user_id")
  @NonNull
  private UUID userId;
}
