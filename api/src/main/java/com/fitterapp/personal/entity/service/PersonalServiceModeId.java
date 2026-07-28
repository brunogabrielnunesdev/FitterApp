package com.fitterapp.personal.entity.service;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalServiceModeId implements Serializable {

  @Column(name = "personal_id")
  private UUID personalId;

  @Enumerated(EnumType.STRING)
  @Column(name = "service_mode", length = 30)
  private ServiceMode serviceMode;

  public PersonalServiceModeId(UUID personalId, ServiceMode serviceMode) {
    this.personalId = Objects.requireNonNull(personalId);
    this.serviceMode = Objects.requireNonNull(serviceMode);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PersonalServiceModeId that)) {
      return false;
    }
    return Objects.equals(personalId, that.personalId) && serviceMode == that.serviceMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(personalId, serviceMode);
  }
}
