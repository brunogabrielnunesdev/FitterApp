package com.fitterapp.academy.domain;

import com.fitterapp.academy.entity.modality.AcademyRevisionModality;
import com.fitterapp.academy.entity.modality.AcademyRevisionModalityId;
import com.fitterapp.academy.entity.profile.AcademyProfileRevision;
import com.fitterapp.personal.entity.modality.Modality;

public final class AcademyRevisionModalityActions {
  private AcademyRevisionModalityActions() {}

  public static AcademyRevisionModality link(AcademyProfileRevision revision, Modality modality) {
    AcademyRevisionModality link = new AcademyRevisionModality();
    link.setId(new AcademyRevisionModalityId(revision.getId(), modality.getId()));
    link.setRevision(revision);
    link.setModality(modality);
    return link;
  }
}
