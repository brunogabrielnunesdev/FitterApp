package com.fitterapp.personal.service.query;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.repository.ProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListProfilesForReviewService {
  private final ProfileRepository profiles;

  @Transactional(readOnly = true)
  public List<Profile> listPending() {
    return profiles.findAllByStatusOrderByUpdatedAtAsc(ProfileStatus.PENDING_REVIEW);
  }
}
