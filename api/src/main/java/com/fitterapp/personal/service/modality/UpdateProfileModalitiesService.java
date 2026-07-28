package com.fitterapp.personal.service.modality;

import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.exception.UnavailableModalityException;
import com.fitterapp.personal.repository.ModalityRepository;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProfileModalitiesService {

  private final ProfileRepository profileRepository;
  private final ModalityRepository modalityRepository;
  private final RevisionModalityRepository revisionModalityRepository;

  @Transactional
  public UpdateProfileModalitiesResult update(UpdateProfileModalitiesCommand command) {
    var profile =
        profileRepository
            .findByIdAndUserId(command.profileId(), command.userId())
            .orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();

    if (revision == null || !isEditable(revision.getStatus())) {
      throw new ProfileRevisionNotEditableException();
    }

    Set<Short> requestedIds = new LinkedHashSet<>(command.modalityIds());
    var modalities = modalityRepository.findAllByIdInAndActiveTrue(requestedIds);
    if (modalities.size() != requestedIds.size()) {
      throw new UnavailableModalityException();
    }

    revisionModalityRepository.deleteByRevisionId(revision.getId());
    List<RevisionModality> links =
        modalities.stream().map(modality -> RevisionModality.link(revision, modality)).toList();
    if (!links.isEmpty()) {
      revisionModalityRepository.saveAll(links);
    }

    return new UpdateProfileModalitiesResult(
        profile.getId(), revision.getId(), List.copyOf(requestedIds));
  }

  private boolean isEditable(ProfileRevisionStatus status) {
    return status == ProfileRevisionStatus.DRAFT || status == ProfileRevisionStatus.REJECTED;
  }
}
