package com.fitterapp.academy.entity.modality;

import com.fitterapp.academy.entity.profile.AcademyProfileRevision;
import com.fitterapp.personal.entity.modality.Modality;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "academy_revision_modalities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademyRevisionModality {

  @EmbeddedId private AcademyRevisionModalityId id;

  @MapsId("revisionId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "revision_id", nullable = false)
  private AcademyProfileRevision revision;

  @MapsId("modalityId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "modality_id", nullable = false)
  private Modality modality;
}
