package com.fitterapp.personal.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.service.publicprofile.PublicProfileDetails;
import java.util.List;
import org.junit.jupiter.api.Test;

class PublicProfileMapperPrivacyTests {

  @Test
  void publicDetailDoesNotReadOrSerializeCrefData() throws Exception {
    Profile profile = mock(Profile.class);
    ProfileRevision revision = mock(ProfileRevision.class);
    var details = new PublicProfileDetails(profile, revision, List.of(), List.of(), List.of());

    var response = new PublicProfileMapper().toDetail(details);
    String json = new ObjectMapper().writeValueAsString(response);

    verify(revision, never()).getCref();
    assertThat(json).doesNotContain("cref", "registrationCode", "documentImageKey");
  }
}
