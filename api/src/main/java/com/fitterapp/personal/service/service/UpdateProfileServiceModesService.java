package com.fitterapp.personal.service.service;

import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProfileServiceModesService {

  private final ProfileRepository profileRepository;
  private final RevisionServiceModeRepository revisionServiceModeRepository;

  @Transactional
  public UpdateProfileServiceModesResult update(UpdateProfileServiceModesCommand command) {
    var profile =
        profileRepository
            .findByIdAndUserId(command.profileId(), command.userId())
            .orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();

    if (revision == null || !isEditable(revision.getStatus())) {
      throw new ProfileRevisionNotEditableException();
    }

    Set<ServiceMode> requestedModes = new LinkedHashSet<>(command.serviceModes());
    revisionServiceModeRepository.deleteByRevisionId(revision.getId());
    List<RevisionServiceMode> modes =
        requestedModes.stream()
            .map(serviceMode -> RevisionServiceMode.of(revision, serviceMode))
            .toList();
    if (!modes.isEmpty()) {
      revisionServiceModeRepository.saveAll(modes);
    }

    return new UpdateProfileServiceModesResult(
        profile.getId(), revision.getId(), List.copyOf(requestedModes));
  }

  private boolean isEditable(ProfileRevisionStatus status) {
    return status == ProfileRevisionStatus.DRAFT || status == ProfileRevisionStatus.REJECTED;
  }
}
