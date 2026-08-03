package com.fitterapp.personal.service.publicprofile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import com.fitterapp.personal.repository.ProfileRepository;
import com.fitterapp.personal.repository.RevisionModalityRepository;
import com.fitterapp.personal.repository.RevisionServiceAreaRepository;
import com.fitterapp.personal.repository.RevisionServiceModeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetPublicProfileServiceTests {

  @Mock private ProfileRepository profiles;

  @Mock private RevisionModalityRepository revisionModalities;

  @Mock private RevisionServiceModeRepository revisionServiceModes;

  @Mock private RevisionServiceAreaRepository revisionServiceAreas;

  @Mock private Profile profile;

  @Mock private ProfileRevision revision;

  @Test
  void returnsPublishedProfileWithItsPublicRelations() {
    UUID revisionId = UUID.randomUUID();
    List<RevisionModality> modalities = List.of();
    List<RevisionServiceMode> serviceModes = List.of();
    List<RevisionServiceArea> serviceAreas = List.of();
    GetPublicProfileService service =
        new GetPublicProfileService(
            profiles, revisionModalities, revisionServiceModes, revisionServiceAreas);
    when(profiles.findPublishedBySlug("bruno-personal")).thenReturn(Optional.of(profile));
    when(profile.getPublishedRevision()).thenReturn(revision);
    when(revision.getId()).thenReturn(revisionId);
    when(revisionModalities.findAllByRevisionIdOrderByModalityNameAsc(revisionId))
        .thenReturn(modalities);
    when(revisionServiceModes.findAllByRevisionIdOrderByIdServiceModeAsc(revisionId))
        .thenReturn(serviceModes);
    when(revisionServiceAreas.findAllByRevisionIdOrderByCityAscNeighborhoodAsc(revisionId))
        .thenReturn(serviceAreas);

    PublicProfileDetails result = service.get("bruno-personal");

    assertThat(result.profile()).isSameAs(profile);
    assertThat(result.revision()).isSameAs(revision);
    assertThat(result.modalities()).isSameAs(modalities);
    verify(revisionModalities).findAllByRevisionIdOrderByModalityNameAsc(revisionId);
    verify(revisionServiceModes).findAllByRevisionIdOrderByIdServiceModeAsc(revisionId);
    verify(revisionServiceAreas).findAllByRevisionIdOrderByCityAscNeighborhoodAsc(revisionId);
  }
}
