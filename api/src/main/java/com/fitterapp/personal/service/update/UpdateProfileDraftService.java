package com.fitterapp.personal.service.update;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.exception.InvalidProfilePriceException;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProfileDraftService {

    private final ProfileRepository profileRepository;
    private final Clock clock;

    @Transactional
    public UpdateProfileDraftResult update(UpdateProfileDraftCommand command) {
        if ((command.startingPriceCents() == null) != (command.priceUnit() == null)) {
            throw new InvalidProfilePriceException();
        }

        var profile = profileRepository.findByIdAndUserId(
                        command.profileId(),
                        command.userId())
                .orElseThrow(ProfileNotFoundException::new);
        var revision = profile.getCurrentRevision();

        if (revision == null || !isEditable(revision.getStatus())) {
            throw new ProfileRevisionNotEditableException();
        }

        OffsetDateTime updatedAt = OffsetDateTime.now(clock);
        revision.updateProfessionalData(
                command.fullName(),
                command.biography(),
                command.whatsapp(),
                command.experienceStartedYear(),
                command.certifications(),
                command.gymsDescription(),
                updatedAt);
        revision.updateStartingPrice(
                command.startingPriceCents(),
                command.priceUnit(),
                updatedAt);

        return new UpdateProfileDraftResult(profile.getId(), revision.getId());
    }

    private boolean isEditable(ProfileRevisionStatus status) {
        return status == ProfileRevisionStatus.DRAFT
                || status == ProfileRevisionStatus.REJECTED;
    }
}
