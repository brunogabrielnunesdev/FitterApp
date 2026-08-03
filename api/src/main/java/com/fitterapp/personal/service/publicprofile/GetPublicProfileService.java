package com.fitterapp.personal.service.publicprofile;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.exception.PublicProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPublicProfileService {
  private final ProfileRepository profiles;
  private final RevisionModalityRepository revisionModalities;
  private final RevisionServiceModeRepository revisionServiceModes;
  private final RevisionServiceAreaRepository revisionServiceAreas;

  @Transactional(readOnly = true)
  public PublicProfileDetails get(String slug) {
    Profile profile =
        profiles.findPublishedBySlug(slug).orElseThrow(PublicProfileNotFoundException::new);
    var revision = profile.getPublishedRevision();

    return new PublicProfileDetails(
        profile,
        revision,
        revisionModalities.findAllByRevisionIdOrderByModalityNameAsc(revision.getId()),
        revisionServiceModes.findAllByRevisionIdOrderByIdServiceModeAsc(revision.getId()),
        revisionServiceAreas.findAllByRevisionIdOrderByCityAscNeighborhoodAsc(revision.getId()));
  }
}
