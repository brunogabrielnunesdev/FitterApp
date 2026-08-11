package com.fitterapp.personal.service.publication;

import com.fitterapp.moderation.exception.ProfileModerationStateException;
import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.personal.exception.*;
import com.fitterapp.personal.repository.ProfileRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfilePublicationService {
  private final ProfileRepository profiles;
  private final Clock clock;

  @Transactional
  public PublicationResult publish(PublishProfileCommand c) {
    var p =
        profiles
            .findByIdAndUserId(c.profileId(), c.userId())
            .orElseThrow(ProfileNotFoundException::new);
    if (p.getStatus() == ProfileStatus.SUSPENDED)
      throw new ProfileModerationStateException("A suspended profile cannot be published");
    if (p.getCurrentRevision() == null
        || p.getCurrentRevision().getStatus() != ProfileRevisionStatus.APPROVED)
      throw new ProfileNotApprovedException();
    p.publish(p.getCurrentRevision(), OffsetDateTime.now(clock));
    return new PublicationResult(p.getId());
  }

  @Transactional
  public PublicationResult unpublish(UnpublishProfileCommand c) {
    var p =
        profiles
            .findByIdAndUserId(c.profileId(), c.userId())
            .orElseThrow(ProfileNotFoundException::new);
    if (p.getStatus() != ProfileStatus.PUBLISHED) throw new ProfileNotApprovedException();
    p.unpublish(OffsetDateTime.now(clock));
    return new PublicationResult(p.getId());
  }
}
