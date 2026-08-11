package com.fitterapp.personal.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import com.fitterapp.personal.exception.ProfileNotFoundException;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAdminProfileServiceTests {

  @Mock private ProfileRepository profiles;
  @Mock private RevisionModalityRepository modalities;
  @Mock private RevisionServiceModeRepository serviceModes;
  @Mock private RevisionServiceAreaRepository serviceAreas;
  @Mock private Profile profile;
  @Mock private ProfileRevision revision;

  private GetAdminProfileService service;

  @BeforeEach
  void setUp() {
    service = new GetAdminProfileService(profiles, modalities, serviceModes, serviceAreas);
  }

  @Test
  void returnsTheCurrentRevisionAndAllAdministrativeRelations() {
    UUID profileId = UUID.randomUUID();
    UUID revisionId = UUID.randomUUID();
    List<RevisionModality> expectedModalities = List.of();
    List<RevisionServiceMode> expectedModes = List.of();
    List<RevisionServiceArea> expectedAreas = List.of();
    when(profiles.findByIdForAdministration(profileId)).thenReturn(Optional.of(profile));
    when(profile.getCurrentRevision()).thenReturn(revision);
    when(revision.getId()).thenReturn(revisionId);
    when(modalities.findAllByRevisionIdOrderByModalityNameAsc(revisionId))
        .thenReturn(expectedModalities);
    when(serviceModes.findAllByRevisionIdOrderByIdServiceModeAsc(revisionId))
        .thenReturn(expectedModes);
    when(serviceAreas.findAllByRevisionIdOrderByCityAscNeighborhoodAsc(revisionId))
        .thenReturn(expectedAreas);

    var result = service.get(profileId);

    assertThat(result.profile()).isSameAs(profile);
    assertThat(result.revision()).isSameAs(revision);
    assertThat(result.modalities()).isSameAs(expectedModalities);
    assertThat(result.serviceModes()).isSameAs(expectedModes);
    assertThat(result.serviceAreas()).isSameAs(expectedAreas);
  }

  @Test
  void rejectsAnUnknownProfile() {
    UUID profileId = UUID.randomUUID();
    when(profiles.findByIdForAdministration(profileId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.get(profileId)).isInstanceOf(ProfileNotFoundException.class);
  }

  @Test
  void rejectsAProfileWithoutCurrentRevision() {
    UUID profileId = UUID.randomUUID();
    when(profiles.findByIdForAdministration(profileId)).thenReturn(Optional.of(profile));
    when(profile.getCurrentRevision()).thenReturn(null);

    assertThatThrownBy(() -> service.get(profileId)).isInstanceOf(ProfileNotFoundException.class);
    verifyNoInteractions(modalities, serviceModes, serviceAreas);
  }
}
