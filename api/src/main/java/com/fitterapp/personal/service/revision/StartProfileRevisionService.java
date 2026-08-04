package com.fitterapp.personal.service.revision;

import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import com.fitterapp.personal.exception.ProfileNotApprovedException;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.ProfileRevisionRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StartProfileRevisionService {
  private final ProfileRepository profiles;
  private final ProfileRevisionRepository revisions;
  private final RevisionModalityRepository modalities;
  private final RevisionServiceModeRepository serviceModes;
  private final RevisionServiceAreaRepository serviceAreas;
  private final Clock clock;

  @Transactional
  public StartProfileRevisionResult start(StartProfileRevisionCommand command) {
    var profile =
        profiles
            .findByIdAndUserId(command.profileId(), command.userId())
            .orElseThrow(ProfileNotFoundException::new);
    var source = profile.getCurrentRevision();
    if (source == null || source.getStatus() != ProfileRevisionStatus.APPROVED) {
      throw new ProfileNotApprovedException();
    }

    OffsetDateTime now = OffsetDateTime.now(clock);
    ProfileRevision draft =
        ProfileRevision.draftFrom(
            profile, source.getVersionNumber() + 1, profile.getUser(), source, now);
    revisions.save(draft);
    profile.setCurrentRevision(draft, now);

    modalities.saveAll(
        modalities.findAllByRevisionIdOrderByModalityNameAsc(source.getId()).stream()
            .map(link -> RevisionModality.link(draft, link.getModality()))
            .toList());
    serviceModes.saveAll(
        serviceModes.findAllByRevisionIdOrderByIdServiceModeAsc(source.getId()).stream()
            .map(mode -> RevisionServiceMode.of(draft, mode.getServiceMode()))
            .toList());
    serviceAreas.saveAll(
        serviceAreas.findAllByRevisionIdOrderByCityAscNeighborhoodAsc(source.getId()).stream()
            .map(
                area ->
                    RevisionServiceArea.create(
                        draft,
                        area.getCity(),
                        area.getStateCode(),
                        area.getNeighborhood(),
                        area.getDescription(),
                        now))
            .toList());
    return new StartProfileRevisionResult(profile.getId(), draft.getId());
  }
}
