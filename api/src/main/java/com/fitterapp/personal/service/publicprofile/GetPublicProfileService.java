package com.fitterapp.personal.service.publicprofile;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.exception.PublicProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPublicProfileService {
  private final ProfileRepository profiles;

  @Transactional(readOnly = true)
  public Profile get(String slug) {
    return profiles.findPublishedBySlug(slug).orElseThrow(PublicProfileNotFoundException::new);
  }
}
