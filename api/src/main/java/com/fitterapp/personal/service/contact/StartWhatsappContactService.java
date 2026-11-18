package com.fitterapp.personal.service.contact;

import com.fitterapp.analytics.service.MarketplaceMetricsService;
import com.fitterapp.personal.exception.PublicProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StartWhatsappContactService {
  private final ProfileRepository profiles;
  private final MarketplaceMetricsService metrics;

  @Transactional
  public StartWhatsappContactResult start(StartWhatsappContactCommand command) {
    var profile =
        profiles
            .findPublishedBySlug(command.slug())
            .orElseThrow(PublicProfileNotFoundException::new);
    metrics.contact(
        command.viewerId(),
        command.source(),
        command.visitorId(),
        command.idempotencyKey(),
        profile.getId());
    String phoneNumber = profile.getPublishedRevision().getWhatsapp().replaceAll("\\D", "");
    return new StartWhatsappContactResult("https://wa.me/" + phoneNumber);
  }
}
