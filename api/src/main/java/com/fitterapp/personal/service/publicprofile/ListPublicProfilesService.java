package com.fitterapp.personal.service.publicprofile;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListPublicProfilesService {
  private final ProfileRepository profiles;

  @Transactional(readOnly = true)
  public Page<Profile> list(
      String query,
      Short modalityId,
      String neighborhood,
      ServiceMode serviceMode,
      Pageable pageable) {
    String normalized = query == null || query.isBlank() ? null : query.trim();
    String normalizedNeighborhood =
        neighborhood == null || neighborhood.isBlank() ? null : neighborhood.trim();
    return profiles.findPublished(
        normalized, modalityId, normalizedNeighborhood, serviceMode, pageable);
  }
}
