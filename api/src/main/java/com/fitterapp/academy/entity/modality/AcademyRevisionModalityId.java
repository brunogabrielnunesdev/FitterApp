package com.fitterapp.academy.entity.modality;

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
public class AcademyRevisionModalityId implements Serializable {

  @Column(name = "revision_id")
  @NonNull
  private UUID revisionId;

  @Column(name = "modality_id")
  @NonNull
  private Short modalityId;
}
