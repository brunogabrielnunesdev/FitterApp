package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.service.cref.UpsertCrefCommand;
import com.fitterapp.personal.service.cref.UpsertCrefService;
import com.fitterapp.personal.service.modality.UpdateProfileModalitiesCommand;
import com.fitterapp.personal.service.modality.UpdateProfileModalitiesService;
import com.fitterapp.personal.service.publication.ProfilePublicationService;
import com.fitterapp.personal.service.publication.PublishProfileCommand;
import com.fitterapp.personal.service.publication.UnpublishProfileCommand;
import com.fitterapp.personal.service.query.GetOwnProfileDraftService;
import com.fitterapp.personal.service.query.GetOwnProfileService;
import com.fitterapp.personal.service.revision.StartProfileRevisionCommand;
import com.fitterapp.personal.service.revision.StartProfileRevisionService;
import com.fitterapp.personal.service.service.UpdateProfileServiceAreasCommand;
import com.fitterapp.personal.service.service.UpdateProfileServiceAreasService;
import com.fitterapp.personal.service.service.UpdateProfileServiceModesCommand;
import com.fitterapp.personal.service.service.UpdateProfileServiceModesService;
import com.fitterapp.personal.service.submission.SubmitProfileForReviewCommand;
import com.fitterapp.personal.service.submission.SubmitProfileForReviewService;
import com.fitterapp.personal.service.update.UpdateProfileDraftCommand;
import com.fitterapp.personal.service.update.UpdateProfileDraftService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class ProfileOwnershipPersistenceTests {
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private GetOwnProfileService getOwnProfileService;
  @Autowired private GetOwnProfileDraftService getOwnProfileDraftService;
  @Autowired private UpdateProfileDraftService updateProfileDraftService;
  @Autowired private UpsertCrefService upsertCrefService;
  @Autowired private UpdateProfileModalitiesService updateProfileModalitiesService;
  @Autowired private UpdateProfileServiceModesService updateProfileServiceModesService;
  @Autowired private UpdateProfileServiceAreasService updateProfileServiceAreasService;
  @Autowired private SubmitProfileForReviewService submitProfileForReviewService;
  @Autowired private StartProfileRevisionService startProfileRevisionService;
  @Autowired private ProfilePublicationService profilePublicationService;

  @Test
  void hidesAndRejectsEveryOwnerScopedOperationForAnotherUsersProfile() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    UUID ownerId = insertUser("profile-owner@fitterapp.test", "+5544999999701", now);
    UUID otherUserId = insertUser("profile-other@fitterapp.test", "+5544999999702", now);
    UUID profileId = insertProfile(ownerId, now);

    assertNotFound(() -> getOwnProfileService.get(otherUserId));
    assertNotFound(() -> getOwnProfileDraftService.get(otherUserId));
    assertNotFound(
        () ->
            updateProfileDraftService.update(
                new UpdateProfileDraftCommand(
                    otherUserId,
                    profileId,
                    "Other User",
                    "Biography",
                    "+5544999999702",
                    null,
                    null,
                    null,
                    null,
                    null)));
    assertNotFound(
        () ->
            upsertCrefService.upsert(
                new UpsertCrefCommand(
                    otherUserId,
                    profileId,
                    "123456-G/PR",
                    "private/crefs/other/document.webp")));
    assertNotFound(
        () ->
            updateProfileModalitiesService.update(
                new UpdateProfileModalitiesCommand(otherUserId, profileId, List.of())));
    assertNotFound(
        () ->
            updateProfileServiceModesService.update(
                new UpdateProfileServiceModesCommand(otherUserId, profileId, List.of())));
    assertNotFound(
        () ->
            updateProfileServiceAreasService.update(
                new UpdateProfileServiceAreasCommand(otherUserId, profileId, List.of())));
    assertNotFound(
        () ->
            submitProfileForReviewService.submit(
                new SubmitProfileForReviewCommand(otherUserId, profileId)));
    assertNotFound(
        () ->
            startProfileRevisionService.start(
                new StartProfileRevisionCommand(otherUserId, profileId)));
    assertNotFound(
        () ->
            profilePublicationService.publish(new PublishProfileCommand(otherUserId, profileId)));
    assertNotFound(
        () ->
            profilePublicationService.unpublish(
                new UnpublishProfileCommand(otherUserId, profileId)));
  }

  private void assertNotFound(org.assertj.core.api.ThrowableAssert.ThrowingCallable operation) {
    assertThatThrownBy(operation).isInstanceOf(ProfileNotFoundException.class);
  }

  private UUID insertUser(String email, String phone, OffsetDateTime now) {
    UUID id = UUID.randomUUID();
    jdbcTemplate.update(
        """
        INSERT INTO users (
            id, full_name, email, phone_number, password_hash, status,
            email_verified_at, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        id,
        "Ownership User",
        email,
        phone,
        "$2a$10$ownership.test.hash",
        "ACTIVE",
        now,
        now,
        now);
    return id;
  }

  private UUID insertProfile(UUID userId, OffsetDateTime now) {
    UUID id = UUID.randomUUID();
    jdbcTemplate.update(
        """
        INSERT INTO personal_profiles (
            id, user_id, slug, status, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?)
        """,
        id,
        userId,
        "ownership-profile",
        "DRAFT",
        now,
        now);
    return id;
  }
}
