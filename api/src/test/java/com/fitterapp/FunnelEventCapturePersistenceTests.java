package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.entity.event.FunnelEventType;
import com.fitterapp.analytics.repository.FunnelEventRepository;
import com.fitterapp.analytics.repository.ProfileViewEventRepository;
import com.fitterapp.analytics.repository.SearchEventRepository;
import com.fitterapp.analytics.service.PublicCatalogEventService;
import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.service.admin.AdminCreatePersonalCommand;
import com.fitterapp.personal.service.admin.AdminPersonalInput;
import com.fitterapp.personal.service.admin.AdminPersonalManagementService;
import com.fitterapp.personal.service.service.ServiceAreaInput;
import com.fitterapp.personal.service.submission.SubmitProfileForReviewCommand;
import com.fitterapp.personal.service.submission.SubmitProfileForReviewService;
import com.fitterapp.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class FunnelEventCapturePersistenceTests {
  @Autowired private EntityManager entityManager;
  @Autowired private AdminPersonalManagementService adminPersonalService;
  @Autowired private SubmitProfileForReviewService submissionService;
  @Autowired private PublicCatalogEventService publicCatalogEventService;
  @Autowired private FunnelEventRepository funnelEvents;
  @Autowired private SearchEventRepository searchEvents;
  @Autowired private ProfileViewEventRepository profileViewEvents;
  @Autowired private ProfileRepository profiles;

  @Test
  void capturesTheCompleteMvpFunnelWithUserSourceAndTimestamp() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    User admin =
        User.pendingRegistration(
            "Funnel Admin",
            "funnel.admin@example.com",
            "+5544999999801",
            "admin-password-hash",
            now);
    admin.confirmEmail(now);
    entityManager.persist(admin);
    entityManager.flush();

    var created =
        adminPersonalService.create(
            new AdminCreatePersonalCommand(
                admin.getId(),
                "Funnel Personal",
                "funnel.personal@example.com",
                "+5544999999802",
                "temporary-password",
                new AdminPersonalInput(
                    "Funnel Personal",
                    "Professional biography",
                    "+5544999999802",
                    (short) 2020,
                    null,
                    null,
                    null,
                    null,
                    List.of((short) 1),
                    List.of(ServiceMode.ONLINE),
                    List.of(new ServiceAreaInput("Umuarama", "PR", null, null)),
                    null,
                    null),
                "Funnel test"));

    submissionService.submit(
        new SubmitProfileForReviewCommand(
            created.userId(), created.profileId(), EventSource.MOBILE_APP));
    var profile = profiles.findById(created.profileId()).orElseThrow();
    publicCatalogEventService.recordSearch(
        created.userId(),
        EventSource.PUBLIC_WEB,
        "Funnel",
        (short) 1,
        "Centro",
        ServiceMode.ONLINE,
        0,
        20,
        1);
    publicCatalogEventService.recordPersonalView(
        created.userId(), EventSource.PUBLIC_WEB, profile);
    entityManager.flush();
    entityManager.clear();

    assertThat(funnelEvents.findAll())
        .extracting(event -> event.getEventType())
        .containsExactlyInAnyOrder(
            FunnelEventType.ACCOUNT_COMPLETED,
            FunnelEventType.PROFILE_STARTED,
            FunnelEventType.PROFILE_SUBMITTED);
    assertThat(funnelEvents.findAll())
        .allSatisfy(
            event -> {
              assertThat(event.getUser().getId()).isEqualTo(created.userId());
              assertThat(event.getOccurredAt()).isNotNull();
            });
    assertThat(funnelEvents.findAll())
        .filteredOn(event -> event.getEventType() == FunnelEventType.ACCOUNT_COMPLETED)
        .singleElement()
        .satisfies(event -> assertThat(event.getSource()).isEqualTo(EventSource.ADMIN_WEB));
    assertThat(funnelEvents.findAll())
        .filteredOn(event -> event.getEventType() == FunnelEventType.PROFILE_SUBMITTED)
        .singleElement()
        .satisfies(event -> assertThat(event.getSource()).isEqualTo(EventSource.MOBILE_APP));

    assertThat(searchEvents.findAll()).singleElement().satisfies(event -> {
      assertThat(event.getUser().getId()).isEqualTo(created.userId());
      assertThat(event.getSource()).isEqualTo(EventSource.PUBLIC_WEB);
      assertThat(event.getSearchTerm()).isEqualTo("Funnel");
      assertThat(event.getFilters().get("modalityId").asInt()).isEqualTo(1);
      assertThat(event.getOccurredAt()).isNotNull();
    });
    assertThat(profileViewEvents.findAll()).singleElement().satisfies(event -> {
      assertThat(event.getViewer().getId()).isEqualTo(created.userId());
      assertThat(event.getPersonalProfile().getId()).isEqualTo(created.profileId());
      assertThat(event.getSource()).isEqualTo(EventSource.PUBLIC_WEB);
      assertThat(event.getOccurredAt()).isNotNull();
    });
  }
}
