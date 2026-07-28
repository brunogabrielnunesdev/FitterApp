package com.fitterapp.personal.service.service;

import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import com.fitterapp.personal.exception.DuplicateServiceAreaException;
import com.fitterapp.personal.exception.InvalidServiceAreaException;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProfileServiceAreasService {

  private final ProfileRepository profileRepository;
  private final RevisionServiceAreaRepository revisionServiceAreaRepository;
  private final Clock clock;

  @Transactional
  public UpdateProfileServiceAreasResult update(UpdateProfileServiceAreasCommand command) {
    var profile =
        profileRepository
            .findByIdAndUserId(command.profileId(), command.userId())
            .orElseThrow(ProfileNotFoundException::new);
    var revision = profile.getCurrentRevision();
    if (revision == null || !isEditable(revision.getStatus())) {
      throw new ProfileRevisionNotEditableException();
    }

    List<ServiceAreaInput> areas = command.serviceAreas().stream().map(this::normalize).toList();
    validateNoDuplicates(areas);

    revisionServiceAreaRepository.deleteByRevisionId(revision.getId());
    OffsetDateTime createdAt = OffsetDateTime.now(clock);
    var entities =
        areas.stream()
            .map(
                area ->
                    RevisionServiceArea.create(
                        revision,
                        area.city(),
                        area.stateCode(),
                        area.neighborhood(),
                        area.description(),
                        createdAt))
            .toList();
    if (!entities.isEmpty()) {
      revisionServiceAreaRepository.saveAll(entities);
    }
    return new UpdateProfileServiceAreasResult(profile.getId(), revision.getId(), areas);
  }

  private ServiceAreaInput normalize(ServiceAreaInput area) {
    String city = trimRequired(area.city());
    String stateCode = trimRequired(area.stateCode()).toUpperCase(Locale.ROOT);
    if (stateCode.length() != 2 || !stateCode.chars().allMatch(Character::isLetter)) {
      throw new InvalidServiceAreaException();
    }
    return new ServiceAreaInput(
        city, stateCode, trimOptional(area.neighborhood()), trimOptional(area.description()));
  }

  private void validateNoDuplicates(List<ServiceAreaInput> areas) {
    Set<String> locations = new HashSet<>();
    for (ServiceAreaInput area : areas) {
      String key =
          (area.stateCode() + '|' + area.city() + '|' + area.neighborhood())
              .toLowerCase(Locale.ROOT);
      if (!locations.add(key)) throw new DuplicateServiceAreaException();
    }
  }

  private String trimRequired(String value) {
    if (value == null || value.isBlank()) throw new InvalidServiceAreaException();
    return value.trim();
  }

  private String trimOptional(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private boolean isEditable(ProfileRevisionStatus status) {
    return status == ProfileRevisionStatus.DRAFT || status == ProfileRevisionStatus.REJECTED;
  }
}
