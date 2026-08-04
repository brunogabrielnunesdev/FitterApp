package com.fitterapp.personal.service.query;

import com.fitterapp.personal.dto.profile.ProfileDraftResponseDto;
import com.fitterapp.personal.dto.profile.ServiceAreaRequestDto;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.repository.CrefRepository;
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
public class GetOwnProfileDraftService {

  private final ProfileRepository profiles;
  private final CrefRepository crefs;
  private final RevisionModalityRepository revisionModalities;
  private final RevisionServiceModeRepository revisionServiceModes;
  private final RevisionServiceAreaRepository revisionServiceAreas;

  @Transactional(readOnly = true)
  public ProfileDraftResponseDto get(UUID userId) {
    var profile = profiles.findByUserId(userId).orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();
    if (revision == null) throw new ProfileNotFoundException();

    var cref = crefs.findByPersonalId(profile.getId()).orElse(null);
    return new ProfileDraftResponseDto(
        profile.getId(),
        revision.getId(),
        profile.getStatus(),
        revision.getStatus(),
        revision.getRejectionReason(),
        revision.getFullName(),
        revision.getBiography(),
        revision.getWhatsapp(),
        revision.getExperienceStartedYear(),
        revision.getCertifications(),
        revision.getGymsDescription(),
        revision.getStartingPriceCents(),
        revision.getPriceUnit(),
        cref == null ? null : cref.getRegistrationCode(),
        cref == null ? null : cref.getDocumentImageKey(),
        cref == null ? null : cref.getStatus(),
        revisionModalities.findAllByRevisionIdOrderByModalityNameAsc(revision.getId()).stream()
            .map(link -> link.getModality().getId())
            .toList(),
        revisionServiceModes.findAllByRevisionIdOrderByIdServiceModeAsc(revision.getId()).stream()
            .map(mode -> mode.getServiceMode())
            .toList(),
        revisionServiceAreas
            .findAllByRevisionIdOrderByCityAscNeighborhoodAsc(revision.getId())
            .stream()
            .map(
                area ->
                    new ServiceAreaRequestDto(
                        area.getCity(),
                        area.getStateCode(),
                        area.getNeighborhood(),
                        area.getDescription()))
            .toList());
  }
}
