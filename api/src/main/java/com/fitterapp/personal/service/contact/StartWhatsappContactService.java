package com.fitterapp.personal.service.contact;

import com.fitterapp.analytics.entity.event.ContactEvent;
import com.fitterapp.analytics.repository.ContactEventRepository;
import com.fitterapp.personal.exception.PublicProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StartWhatsappContactService {

  private final ProfileRepository profiles;
  private final UserRepository users;
  private final ContactEventRepository contactEvents;
  private final Clock clock;

  @Transactional
  public StartWhatsappContactResult start(StartWhatsappContactCommand command) {
    var profile =
        profiles
            .findPublishedBySlug(command.slug())
            .orElseThrow(PublicProfileNotFoundException::new);
    var viewer =
        command.viewerId() == null ? null : users.findById(command.viewerId()).orElse(null);
    OffsetDateTime now = OffsetDateTime.now(clock);

    contactEvents.save(
        ContactEvent.whatsappToPersonal(viewer, profile, command.source(), null, now));

    String phoneNumber = profile.getPublishedRevision().getWhatsapp().replaceAll("\\D", "");
    return new StartWhatsappContactResult("https://wa.me/" + phoneNumber);
  }
}
