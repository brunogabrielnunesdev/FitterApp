package com.fitterapp.personal.service.contact;

import com.fitterapp.analytics.entity.event.ContactEvent;
import com.fitterapp.analytics.repository.ContactEventRepository;
import com.fitterapp.analytics.service.MetricDeduplicationService;
import com.fitterapp.analytics.service.MetricEventType;
import com.fitterapp.personal.exception.PublicProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.repository.UserRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
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
  private final MetricDeduplicationService deduplication;

  @Transactional
  public StartWhatsappContactResult start(StartWhatsappContactCommand command) {
    var profile =
        profiles
            .findPublishedBySlug(command.slug())
            .orElseThrow(PublicProfileNotFoundException::new);
    var viewer =
        command.viewerId() == null ? null : users.findById(command.viewerId()).orElse(null);
    OffsetDateTime now = OffsetDateTime.now(clock);
    var decision =
        deduplication.evaluate(
            MetricEventType.WHATSAPP_CONTACT,
            command.viewerId(),
            command.visitorId(),
            command.idempotencyKey(),
            now,
            List.of(command.source().name(), String.valueOf(profile.getId())));
    if (decision.recordEvent()) {
      contactEvents.save(
          ContactEvent.whatsappToPersonal(
              viewer,
              profile,
              command.source(),
              null,
              now,
              decision.uniqueEvent(),
              decision.idempotencyKeyHash()));
    }

    String phoneNumber = profile.getPublishedRevision().getWhatsapp().replaceAll("\\D", "");
    return new StartWhatsappContactResult("https://wa.me/" + phoneNumber);
  }
}
