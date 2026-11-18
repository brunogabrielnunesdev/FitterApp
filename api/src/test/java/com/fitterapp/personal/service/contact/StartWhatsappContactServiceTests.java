package com.fitterapp.personal.service.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.service.MarketplaceMetricsService;
import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.personal.repository.ProfileRepository;
import java.util.*;
import org.junit.jupiter.api.Test;

class StartWhatsappContactServiceTests {
  @Test
  void recordsContactAndReturnsNormalizedUrl() {
    var profiles = mock(ProfileRepository.class);
    var metrics = mock(MarketplaceMetricsService.class);
    var profile = mock(Profile.class);
    var revision = mock(ProfileRevision.class);
    var id = UUID.randomUUID();
    when(profiles.findPublishedBySlug("bruno")).thenReturn(Optional.of(profile));
    when(profile.getId()).thenReturn(id);
    when(profile.getPublishedRevision()).thenReturn(revision);
    when(revision.getWhatsapp()).thenReturn("+55 (44) 99999-9999");
    var result =
        new StartWhatsappContactService(profiles, metrics)
            .start(
                new StartWhatsappContactCommand(
                    "bruno", null, EventSource.MOBILE_APP, "visitor", "intent"));
    var url = java.net.URI.create(result.whatsappUrl());
    assertThat(url.getHost()).isEqualTo("wa.me");
    assertThat(url.getPath()).isEqualTo("/5544999999999");
    assertThat(
            java.net.URLDecoder.decode(url.getRawQuery(), java.nio.charset.StandardCharsets.UTF_8))
        .isEqualTo(
            "text=Olá! Encontrei seu perfil no FitterApp e gostaria de saber mais sobre seus treinos.");
    verify(metrics).contact(null, EventSource.MOBILE_APP, "visitor", "intent", id);
  }
}
