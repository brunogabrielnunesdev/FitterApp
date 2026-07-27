package com.fitterapp.personal.service.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
import com.fitterapp.personal.entity.service.PriceUnit;
import com.fitterapp.personal.exception.InvalidProfilePriceException;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.user.entity.User;

@ExtendWith(MockitoExtension.class)
class UpdateProfileDraftServiceTests {

    private static final Instant NOW = Instant.parse("2026-07-27T16:00:00Z");

    @Mock
    private ProfileRepository profileRepository;

    private UpdateProfileDraftService service;

    @BeforeEach
    void setUp() {
        service = new UpdateProfileDraftService(
                profileRepository,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void updatesDraftProfessionalData() {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        UUID revisionId = UUID.randomUUID();
        Profile profile = profile(userId, profileId, revisionId);
        when(profileRepository.findByIdAndUserId(profileId, userId))
                .thenReturn(Optional.of(profile));

        UpdateProfileDraftResult result = service.update(command(userId, profileId));

        ProfileRevision revision = profile.getCurrentRevision();
        assertThat(revision.getFullName()).isEqualTo("Bruno Gabriel Personal");
        assertThat(revision.getBiography()).isEqualTo("Treinador com foco em hipertrofia.");
        assertThat(revision.getWhatsapp()).isEqualTo("+5544999999999");
        assertThat(revision.getExperienceStartedYear()).isEqualTo((short) 2018);
        assertThat(revision.getCertifications()).isEqualTo("CREF e pos-graduacao");
        assertThat(revision.getGymsDescription()).isEqualTo("Atende na Academia Centro");
        assertThat(revision.getStartingPriceCents()).isEqualTo(12000);
        assertThat(revision.getPriceUnit()).isEqualTo(PriceUnit.PER_SESSION);
        assertThat(revision.getUpdatedAt().toInstant()).isEqualTo(NOW);
        assertThat(result).isEqualTo(new UpdateProfileDraftResult(profileId, revisionId));
    }

    @Test
    void allowsCorrectionOfRejectedRevision() {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        Profile profile = profile(userId, profileId, UUID.randomUUID());
        ProfileRevision revision = profile.getCurrentRevision();
        User reviewer = user(userId);
        revision.submit(NOW.atOffset(ZoneOffset.UTC));
        revision.reject(reviewer, "Ajuste a biografia", NOW.atOffset(ZoneOffset.UTC));
        when(profileRepository.findByIdAndUserId(profileId, userId))
                .thenReturn(Optional.of(profile));

        service.update(command(userId, profileId));

        assertThat(revision.getStatus()).isEqualTo(ProfileRevisionStatus.REJECTED);
        assertThat(revision.getBiography()).isEqualTo("Treinador com foco em hipertrofia.");
    }

    @Test
    void rejectsPartialPriceInformationBeforeLoadingProfile() {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        assertThatThrownBy(() -> service.update(new UpdateProfileDraftCommand(
                userId,
                profileId,
                "Bruno",
                null,
                null,
                null,
                null,
                null,
                10000,
                null)))
                .isInstanceOf(InvalidProfilePriceException.class);

        verify(profileRepository, never()).findByIdAndUserId(profileId, userId);
    }

    @Test
    void rejectsEditingRevisionThatIsAwaitingReview() {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        Profile profile = profile(userId, profileId, UUID.randomUUID());
        profile.getCurrentRevision().submit(NOW.atOffset(ZoneOffset.UTC));
        when(profileRepository.findByIdAndUserId(profileId, userId))
                .thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> service.update(command(userId, profileId)))
                .isInstanceOf(ProfileRevisionNotEditableException.class);
    }

    @Test
    void rejectsProfileThatDoesNotBelongToApplicant() {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(profileRepository.findByIdAndUserId(profileId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(command(userId, profileId)))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    private UpdateProfileDraftCommand command(UUID userId, UUID profileId) {
        return new UpdateProfileDraftCommand(
                userId,
                profileId,
                "Bruno Gabriel Personal",
                "Treinador com foco em hipertrofia.",
                "+5544999999999",
                (short) 2018,
                "CREF e pos-graduacao",
                "Atende na Academia Centro",
                12000,
                PriceUnit.PER_SESSION);
    }

    private Profile profile(UUID userId, UUID profileId, UUID revisionId) {
        User user = user(userId);
        Profile profile = Profile.draft(
                "Bruno Gabriel",
                "bruno-gabriel",
                NOW.atOffset(ZoneOffset.UTC));
        ReflectionTestUtils.setField(profile, "id", profileId);
        profile.linkUser(user, NOW.atOffset(ZoneOffset.UTC));
        ProfileRevision revision = ProfileRevision.draft(
                profile,
                1,
                user,
                true,
                NOW.atOffset(ZoneOffset.UTC));
        ReflectionTestUtils.setField(revision, "id", revisionId);
        profile.setCurrentRevision(revision, NOW.atOffset(ZoneOffset.UTC));
        return profile;
    }

    private User user(UUID userId) {
        User user = User.pendingRegistration(
                "Bruno Gabriel",
                "bruno@fitterapp.com",
                "+5544999999999",
                "password-hash",
                NOW.atOffset(ZoneOffset.UTC));
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
