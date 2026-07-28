package com.fitterapp.personal.entity.service;

import com.fitterapp.personal.entity.profile.Profile;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personal_service_modes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalServiceMode {

  @EmbeddedId private PersonalServiceModeId id;

  @MapsId("personalId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "personal_id", nullable = false)
  private Profile personal;

  public static PersonalServiceMode of(Profile personal, ServiceMode serviceMode) {
    PersonalServiceMode mode = new PersonalServiceMode();
    mode.id = new PersonalServiceModeId(personal.getId(), serviceMode);
    mode.personal = personal;
    return mode;
  }

  public ServiceMode getServiceMode() {
    return id.getServiceMode();
  }
}
