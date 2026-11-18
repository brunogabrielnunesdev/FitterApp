package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.service.AdminDashboardService;
import com.fitterapp.analytics.service.MarketplaceMetricsService;
import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.personal.entity.service.ServiceMode;
import jakarta.persistence.EntityManager;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@Import({
  TestcontainersConfiguration.class,
  MarketplaceMetricsService.class,
  AdminDashboardService.class,
  MarketplaceMetricsIntegrationTests.Dependencies.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MarketplaceMetricsIntegrationTests {
  @TestConfiguration
  static class Dependencies {
    @Bean
    Clock clock() {
      return Clock.fixed(Instant.parse("2026-11-18T15:00:00Z"), ZoneOffset.UTC);
    }

    @Bean
    ObjectMapper objectMapper() {
      return new ObjectMapper();
    }
  }

  @Autowired MarketplaceMetricsService metrics;
  @Autowired AdminDashboardService dashboard;
  @Autowired JdbcTemplate jdbc;
  @Autowired EntityManager entityManager;

  @Test
  void deduplicatesEventsAndCountsFiltersReturnsAndReviewHistory() {
    var now = OffsetDateTime.parse("2026-11-18T15:00:00Z");
    var profile = Profile.draft("Ana", "ana-metrics", now);
    entityManager.persist(profile);
    entityManager.flush();
    var revision = ProfileRevision.draft(profile, 1, null, true, now);
    entityManager.persist(revision);
    entityManager.flush();
    revision.submit(now.plusMinutes(1));
    entityManager.flush();
    revision.approve(null, now.plusMinutes(2));
    entityManager.flush();
    metrics.search(
        null,
        EventSource.MOBILE_APP,
        "visitor1",
        "search1",
        "ana",
        (short) 1,
        "Centro",
        ServiceMode.ONLINE,
        1);
    metrics.search(
        null,
        EventSource.MOBILE_APP,
        "visitor1",
        "search1",
        "ana",
        (short) 1,
        "Centro",
        ServiceMode.ONLINE,
        1);
    metrics.profileView(null, EventSource.MOBILE_APP, "visitor1", "view1", profile.getId());
    metrics.profileView(null, EventSource.MOBILE_APP, "visitor1", "view1", profile.getId());
    metrics.contact(null, EventSource.MOBILE_APP, "visitor1", "contact1", profile.getId());
    metrics.contact(null, EventSource.MOBILE_APP, "visitor1", "contact1", profile.getId());
    metrics.appOpen(null, EventSource.MOBILE_APP, "visitor1", "open1");
    metrics.appOpen(null, EventSource.MOBILE_APP, "visitor1", "open1");
    metrics.appOpen(null, EventSource.MOBILE_APP, "visitor1", "open2");
    assertThat(
            jdbc.queryForObject(
                "select count(*) from app_access_events where event_type='RETURNED'", Long.class))
        .isEqualTo(1);
    var data =
        dashboard.get(
            LocalDate.parse("2026-11-18"), LocalDate.parse("2026-11-18"), "America/Sao_Paulo");
    assertThat(data.searches().raw()).isEqualTo(1);
    assertThat(data.searches().unique()).isEqualTo(1);
    assertThat(data.profileViews().raw()).isEqualTo(1);
    assertThat(data.whatsappContacts().raw()).isEqualTo(1);
    assertThat(data.returns().raw()).isEqualTo(1);
    assertThat(data.filters().modality()).isEqualTo(1);
    assertThat(data.filters().neighborhood()).isEqualTo(1);
    assertThat(data.filters().serviceMode()).isEqualTo(1);
    assertThat(data.funnel().profilesSubmitted()).isEqualTo(1);
    assertThat(data.funnel().profilesApproved()).isEqualTo(1);
  }
}
