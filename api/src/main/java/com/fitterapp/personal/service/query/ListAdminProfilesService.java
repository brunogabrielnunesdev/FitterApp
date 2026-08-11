package com.fitterapp.personal.service.query;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListAdminProfilesService {

  private final ProfileRepository profiles;

  @Transactional(readOnly = true)
  public Page<Profile> list(ProfileStatus status, Pageable pageable) {
    return profiles.findAllForAdministration(status, pageable);
  }
}
