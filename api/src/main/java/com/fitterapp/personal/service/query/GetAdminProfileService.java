package com.fitterapp.personal.service.query;

import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAdminProfileService {

  private final ProfileRepository profiles;
  private final RevisionModalityRepository revisionModalities;
  private final RevisionServiceModeRepository revisionServiceModes;
  private final RevisionServiceAreaRepository revisionServiceAreas;

  @Transactional(readOnly = true)
  public AdminProfileDetails get(UUID profileId) {
    var profile =
        profiles.findByIdForAdministration(profileId).orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();
    if (revision == null) {
      throw new ProfileNotFoundException();
    }

    return new AdminProfileDetails(
        profile,
        revision,
        revisionModalities.findAllByRevisionIdOrderByModalityNameAsc(revision.getId()),
        revisionServiceModes.findAllByRevisionIdOrderByIdServiceModeAsc(revision.getId()),
        revisionServiceAreas.findAllByRevisionIdOrderByCityAscNeighborhoodAsc(revision.getId()));
  }
}
