package com.fitterapp.personal.service.query;

import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetOwnProfileService {
  private final ProfileRepository profiles;

  @Transactional(readOnly = true)
  public com.fitterapp.personal.entity.profile.Profile get(UUID userId) {
    return profiles.findByUserId(userId).orElseThrow(ProfileNotFoundException::new);
  }
}
