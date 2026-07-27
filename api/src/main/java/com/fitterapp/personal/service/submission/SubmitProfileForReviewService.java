package com.fitterapp.personal.service.submission;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.exception.IncompleteProfileException;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmitProfileForReviewService {
    private final ProfileRepository profileRepository;
    private final RevisionModalityRepository revisionModalityRepository;
    private final RevisionServiceModeRepository revisionServiceModeRepository;
    private final RevisionServiceAreaRepository revisionServiceAreaRepository;
    private final Clock clock;

    @Transactional
    public SubmitProfileForReviewResult submit(SubmitProfileForReviewCommand command) {
        var profile = profileRepository.findByIdAndUserId(command.profileId(), command.userId())
                .orElseThrow(ProfileNotFoundException::new);
        var revision = profile.getCurrentRevision();
        if (revision == null || (revision.getStatus() != ProfileRevisionStatus.DRAFT
                && revision.getStatus() != ProfileRevisionStatus.REJECTED)) {
            throw new ProfileRevisionNotEditableException();
        }
        if (!isComplete(revision)) throw new IncompleteProfileException();
        revision.submit(OffsetDateTime.now(clock));
        return new SubmitProfileForReviewResult(profile.getId(), revision.getId());
    }

    private boolean isComplete(com.fitterapp.personal.entity.profile.ProfileRevision revision) {
        return hasText(revision.getFullName()) && hasText(revision.getBiography()) && hasText(revision.getWhatsapp())
                && revision.getCref() != null && hasText(revision.getCref().getDocumentImageKey())
                && revisionModalityRepository.countByIdRevisionId(revision.getId()) > 0
                && revisionServiceModeRepository.countByIdRevisionId(revision.getId()) > 0
                && revisionServiceAreaRepository.countByRevisionId(revision.getId()) > 0;
    }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}
