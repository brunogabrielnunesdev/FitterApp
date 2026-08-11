package com.fitterapp.personal.service.submission;

import com.fitterapp.analytics.entity.event.FunnelEvent;
import com.fitterapp.analytics.repository.FunnelEventRepository;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.exception.IncompleteProfileException;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.ProfileRepository;
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
public class SubmitProfileForReviewService {
  private final ProfileRepository profileRepository;
  private final RevisionModalityRepository revisionModalityRepository;
  private final RevisionServiceModeRepository revisionServiceModeRepository;
  private final RevisionServiceAreaRepository revisionServiceAreaRepository;
  private final Clock clock;
  private final FunnelEventRepository funnelEvents;

  @Transactional
  public SubmitProfileForReviewResult submit(SubmitProfileForReviewCommand command) {
    var profile =
        profileRepository
            .findByIdAndUserId(command.profileId(), command.userId())
            .orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();
    if (revision == null
        || (revision.getStatus() != ProfileRevisionStatus.DRAFT
            && revision.getStatus() != ProfileRevisionStatus.REJECTED)) {
      throw new ProfileRevisionNotEditableException();
    }
    if (!isComplete(revision)) throw new IncompleteProfileException();
    OffsetDateTime now = OffsetDateTime.now(clock);
    revision.submit(now);
    profile.submitForReview(now);
    funnelEvents.save(
        FunnelEvent.profileSubmitted(profile.getUser(), profile, command.source(), now));
    return new SubmitProfileForReviewResult(profile.getId(), revision.getId());
  }

  private boolean isComplete(com.fitterapp.personal.entity.profile.ProfileRevision revision) {
    return hasText(revision.getFullName())
        && hasText(revision.getBiography())
        && hasText(revision.getWhatsapp())
        && revisionModalityRepository.countByIdRevisionId(revision.getId()) > 0
        && revisionServiceModeRepository.countByIdRevisionId(revision.getId()) > 0
        && revisionServiceAreaRepository.countByRevisionId(revision.getId()) > 0;
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
