package com.fitterapp.personal.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.repository.ProfileRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ListAdminProfilesServiceTests {

  @Mock private ProfileRepository profiles;

  @Test
  void listsProfilesUsingTheRequestedStatusAndPage() {
    var pageable = PageRequest.of(2, 20);
    var expected = new PageImpl<Profile>(List.of(), pageable, 45);
    when(profiles.findAllForAdministration(ProfileStatus.PENDING_REVIEW, pageable))
        .thenReturn(expected);

    var result =
        new ListAdminProfilesService(profiles).list(ProfileStatus.PENDING_REVIEW, pageable);

    assertThat(result).isSameAs(expected);
    verify(profiles).findAllForAdministration(ProfileStatus.PENDING_REVIEW, pageable);
  }

  @Test
  void acceptsNoStatusFilter() {
    var pageable = PageRequest.of(0, 20);
    Page<Profile> expected = Page.empty(pageable);
    when(profiles.findAllForAdministration(null, pageable)).thenReturn(expected);

    assertThat(new ListAdminProfilesService(profiles).list(null, pageable)).isSameAs(expected);
  }
}
