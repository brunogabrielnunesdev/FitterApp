package com.fitterapp.personal.entity.modality;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalModalityId implements Serializable {

  @Column(name = "personal_id")
  private UUID personalId;

  @Column(name = "modality_id")
  private Short modalityId;

  public PersonalModalityId(UUID personalId, Short modalityId) {
    this.personalId = Objects.requireNonNull(personalId);
    this.modalityId = Objects.requireNonNull(modalityId);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PersonalModalityId that)) {
      return false;
    }
    return Objects.equals(personalId, that.personalId)
        && Objects.equals(modalityId, that.modalityId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(personalId, modalityId);
  }
}
