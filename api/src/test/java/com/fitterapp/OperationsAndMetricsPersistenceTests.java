package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitterapp.analytics.entity.audit.AdminAuditLog;
import com.fitterapp.analytics.entity.audit.AuditTargetType;
import com.fitterapp.analytics.entity.event.AppAccessEvent;
import com.fitterapp.analytics.entity.event.AppAccessEventType;
import com.fitterapp.analytics.entity.event.ContactEvent;
import com.fitterapp.analytics.entity.event.EventSource;
import com.fitterapp.analytics.entity.event.ProfileViewEvent;
import com.fitterapp.analytics.entity.event.SearchEvent;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OperationsAndMetricsPersistenceTests {

  @Autowired private EntityManager entityManager;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void persistsImmutableAuditLogWithStateSnapshots() throws Exception {
    OffsetDateTime createdAt = now();
    User admin =
        activeUser("Audit Owner", "audit-owner@fitterapp.com", "+5544999999940", createdAt);
    UUID targetId = UUID.randomUUID();

    AdminAuditLog log =
        AdminAuditLog.record(
            admin,
            "ACCOUNT_BLOCKED",
            AuditTargetType.USER,
            targetId,
            "Fraude confirmada",
            null,
            objectMapper.readTree("{\"status\":\"ACTIVE\"}"),
            objectMapper.readTree("{\"status\":\"BLOCKED\"}"),
            createdAt);
    entityManager.persist(log);
    entityManager.flush();
    entityManager.clear();

    AdminAuditLog saved = entityManager.find(AdminAuditLog.class, log.getId());

    assertThat(saved.getActor().getId()).isEqualTo(admin.getId());
    assertThat(saved.getTargetType()).isEqualTo(AuditTargetType.USER);
    assertThat(saved.getTargetId()).isEqualTo(targetId);
    assertThat(saved.getPreviousState().path("status").asText()).isEqualTo("ACTIVE");
    assertThat(saved.getNewState().path("status").asText()).isEqualTo("BLOCKED");
  }

  @Test
  void persistsSearchAndPersonalMarketplaceEvents() {
    OffsetDateTime occurredAt = now();
    User student =
        activeUser(
            "Analytics Student", "analytics-student@fitterapp.com", "+5544999999941", occurredAt);
    Profile personal = personal(student, "analytics-personal", occurredAt);

    SearchEvent search =
        SearchEvent.record(
            student,
            EventSource.MOBILE_APP,
            "musculacao",
            objectMapper.createObjectNode().put("city", "Umuarama").put("serviceMode", "IN_PERSON"),
            8,
            occurredAt);
    ProfileViewEvent view =
        ProfileViewEvent.personalView(
            student, personal, EventSource.MOBILE_APP, "Umuarama", occurredAt.plusMinutes(1));
    ContactEvent contact =
        ContactEvent.whatsappToPersonal(
            student, personal, EventSource.MOBILE_APP, "Umuarama", occurredAt.plusMinutes(2));
    entityManager.persist(search);
    entityManager.persist(view);
    entityManager.persist(contact);
    entityManager.flush();
    entityManager.clear();

    SearchEvent savedSearch = entityManager.find(SearchEvent.class, search.getId());
    ProfileViewEvent savedView = entityManager.find(ProfileViewEvent.class, view.getId());
    ContactEvent savedContact = entityManager.find(ContactEvent.class, contact.getId());

    assertThat(savedSearch.getFilters().path("city").asText()).isEqualTo("Umuarama");
    assertThat(savedSearch.getResultCount()).isEqualTo(8);
    assertThat(savedView.getPersonalProfile().getId()).isEqualTo(personal.getId());
    assertThat(savedView.getAcademyProfile()).isNull();
    assertThat(savedContact.getPersonalProfile().getId()).isEqualTo(personal.getId());
    assertThat(savedContact.getUser().getId()).isEqualTo(student.getId());
  }

  @Test
  void persistsAnonymizedAccessAndContactEvents() {
    OffsetDateTime occurredAt = now();
    User owner =
        activeUser(
            "Anonymous Contact Personal",
            "anonymous-contact@fitterapp.com",
            "+5544999999942",
            occurredAt);
    Profile personal = personal(owner, "anonymous-contact", occurredAt);

    AppAccessEvent access = AppAccessEvent.returned(null, EventSource.PUBLIC_WEB, occurredAt);
    ContactEvent contact =
        ContactEvent.whatsappToPersonal(
            null, personal, EventSource.PUBLIC_WEB, "Maringa", occurredAt);
    entityManager.persist(access);
    entityManager.persist(contact);
    entityManager.flush();
    entityManager.clear();

    AppAccessEvent savedAccess = entityManager.find(AppAccessEvent.class, access.getId());
    ContactEvent savedContact = entityManager.find(ContactEvent.class, contact.getId());

    assertThat(savedAccess.getEventType()).isEqualTo(AppAccessEventType.RETURNED);
    assertThat(savedAccess.getUser()).isNull();
    assertThat(savedContact.getUser()).isNull();
    assertThat(savedContact.getCity()).isEqualTo("Maringa");
  }

  private User activeUser(String fullName, String email, String phone, OffsetDateTime createdAt) {
    User user = User.pendingRegistration(fullName, email, phone, "test-password-hash", createdAt);
    user.confirmEmail(createdAt);
    entityManager.persist(user);
    return user;
  }

  private Profile personal(User user, String slug, OffsetDateTime createdAt) {
    Profile profile = Profile.draft(user.getFullName(), slug, createdAt);
    profile.linkUser(user, createdAt);
    entityManager.persist(profile);
    return profile;
  }

  private OffsetDateTime now() {
    return OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
  }
}
