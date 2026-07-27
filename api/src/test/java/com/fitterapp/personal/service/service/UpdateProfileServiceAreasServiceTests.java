package com.fitterapp.personal.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import com.fitterapp.personal.exception.DuplicateServiceAreaException;
import com.fitterapp.personal.exception.InvalidServiceAreaException;
import com.fitterapp.personal.exception.ProfileRevisionNotEditableException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import com.fitterapp.user.entity.User;

@ExtendWith(MockitoExtension.class)
class UpdateProfileServiceAreasServiceTests {
    private static final Instant NOW = Instant.parse("2026-07-27T19:00:00Z");
    @Mock ProfileRepository profileRepository;
    @Mock RevisionServiceAreaRepository revisionServiceAreaRepository;

    @Test
    void replacesAreasWithNormalizedValues() {
        Fixture f = fixture();
        when(profileRepository.findByIdAndUserId(f.profileId, f.userId)).thenReturn(Optional.of(f.profile));

        var result = service().update(new UpdateProfileServiceAreasCommand(f.userId, f.profileId,
                List.of(new ServiceAreaInput(" Umuarama ", " pr ", " Centro ", " Atende em academias locais "))));

        verify(revisionServiceAreaRepository).deleteByRevisionId(f.revisionId);
        ArgumentCaptor<Iterable<RevisionServiceArea>> captor = areasCaptor();
        verify(revisionServiceAreaRepository).saveAll(captor.capture());
        RevisionServiceArea area = captor.getValue().iterator().next();
        assertThat(area.getCity()).isEqualTo("Umuarama");
        assertThat(area.getStateCode()).isEqualTo("PR");
        assertThat(area.getNeighborhood()).isEqualTo("Centro");
        assertThat(result.serviceAreas()).containsExactly(new ServiceAreaInput(
                "Umuarama", "PR", "Centro", "Atende em academias locais"));
    }

    @Test
    void rejectsInvalidOrDuplicatedAreasBeforeChangingSelection() {
        Fixture f = fixture();
        when(profileRepository.findByIdAndUserId(f.profileId, f.userId)).thenReturn(Optional.of(f.profile));
        assertThatThrownBy(() -> service().update(new UpdateProfileServiceAreasCommand(f.userId, f.profileId,
                List.of(new ServiceAreaInput("", "PR", null, null))))).isInstanceOf(InvalidServiceAreaException.class);
        assertThatThrownBy(() -> service().update(new UpdateProfileServiceAreasCommand(f.userId, f.profileId,
                List.of(new ServiceAreaInput("Umuarama", "PR", null, null), new ServiceAreaInput("umuarama", "pr", null, null)))))
                .isInstanceOf(DuplicateServiceAreaException.class);
        verify(revisionServiceAreaRepository, never()).deleteByRevisionId(any());
    }

    @Test
    void rejectsRevisionAwaitingReview() {
        Fixture f = fixture(); f.revision.submit(NOW.atOffset(ZoneOffset.UTC));
        when(profileRepository.findByIdAndUserId(f.profileId, f.userId)).thenReturn(Optional.of(f.profile));
        assertThatThrownBy(() -> service().update(new UpdateProfileServiceAreasCommand(f.userId, f.profileId, List.of())))
                .isInstanceOf(ProfileRevisionNotEditableException.class);
    }

    private UpdateProfileServiceAreasService service() { return new UpdateProfileServiceAreasService(profileRepository, revisionServiceAreaRepository, Clock.fixed(NOW, ZoneOffset.UTC)); }
    private Fixture fixture() {
        UUID userId = UUID.randomUUID(), profileId = UUID.randomUUID(), revisionId = UUID.randomUUID();
        User user = User.pendingRegistration("Bruno", "bruno@fitterapp.com", "+5544999999999", "hash", NOW.atOffset(ZoneOffset.UTC)); ReflectionTestUtils.setField(user, "id", userId);
        Profile profile = Profile.draft("Bruno", "bruno", NOW.atOffset(ZoneOffset.UTC)); ReflectionTestUtils.setField(profile, "id", profileId); profile.linkUser(user, NOW.atOffset(ZoneOffset.UTC));
        ProfileRevision revision = ProfileRevision.draft(profile, 1, user, true, NOW.atOffset(ZoneOffset.UTC)); ReflectionTestUtils.setField(revision, "id", revisionId); profile.setCurrentRevision(revision, NOW.atOffset(ZoneOffset.UTC));
        return new Fixture(userId, profileId, revisionId, profile, revision);
    }
    @SuppressWarnings({ "unchecked", "rawtypes" }) private ArgumentCaptor<Iterable<RevisionServiceArea>> areasCaptor() { return (ArgumentCaptor) ArgumentCaptor.forClass(Iterable.class); }
    private record Fixture(UUID userId, UUID profileId, UUID revisionId, Profile profile, ProfileRevision revision) { }
}
