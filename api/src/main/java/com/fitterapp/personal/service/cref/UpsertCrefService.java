package com.fitterapp.personal.service.cref;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitterapp.personal.entity.cref.Cref;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.exception.CrefAlreadyInUseException;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.CrefRepository;
import com.fitterapp.personal.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpsertCrefService {

    private final ProfileRepository profileRepository;
    private final CrefRepository crefRepository;
    private final Clock clock;

    @Transactional
    public UpsertCrefResult upsert(UpsertCrefCommand command) {
        var profile = profileRepository.findByIdAndUserId(
                        command.profileId(),
                        command.userId())
                .orElseThrow(ProfileNotFoundException::new);
        var revision = profile.getCurrentRevision();

        if (revision == null || !isEditable(revision.getStatus())) {
            throw new ProfileRevisionNotEditableException();
        }

        String registrationCode = normalizeRegistrationCode(command.registrationCode());
        crefRepository.findByRegistrationCode(registrationCode)
                .filter(existing -> !existing.getPersonal().getId().equals(profile.getId()))
                .ifPresent(existing -> {
                    throw new CrefAlreadyInUseException();
                });

        OffsetDateTime updatedAt = OffsetDateTime.now(clock);
        Cref cref = crefRepository.findByPersonalId(profile.getId())
                .map(existing -> update(existing, registrationCode, command.documentImageKey(), updatedAt))
                .orElseGet(() -> create(profile, registrationCode, command.documentImageKey(), updatedAt));

        revision.assignCref(cref, updatedAt);
        return new UpsertCrefResult(profile.getId(), cref.getId());
    }

    private Cref create(
            com.fitterapp.personal.entity.profile.Profile profile,
            String registrationCode,
            String documentImageKey,
            OffsetDateTime createdAt) {
        Cref cref = Cref.pendingReview(
                profile,
                registrationCode,
                documentImageKey,
                createdAt);
        return crefRepository.save(cref);
    }

    private Cref update(
            Cref cref,
            String registrationCode,
            String documentImageKey,
            OffsetDateTime updatedAt) {
        cref.resubmit(registrationCode, documentImageKey, updatedAt);
        return cref;
    }

    private String normalizeRegistrationCode(String registrationCode) {
        return registrationCode.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isEditable(ProfileRevisionStatus status) {
        return status == ProfileRevisionStatus.DRAFT
                || status == ProfileRevisionStatus.REJECTED;
    }
}
