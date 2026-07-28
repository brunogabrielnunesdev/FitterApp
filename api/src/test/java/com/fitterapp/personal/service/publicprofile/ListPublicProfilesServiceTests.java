package com.fitterapp.personal.service.publicprofile;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.service.ServiceMode;
import com.fitterapp.personal.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ListPublicProfilesServiceTests {

  @Mock private ProfileRepository profiles;

  @Test
  void normalizesSearchAndNeighborhoodBeforeQuerying() {
    var pageable = PageRequest.of(0, 20);
    when(profiles.findPublished("Bruno", (short) 2, "Centro", ServiceMode.ONLINE, pageable))
        .thenReturn(new PageImpl<>(java.util.List.of(), pageable, 0));

    new ListPublicProfilesService(profiles)
        .list(" Bruno ", (short) 2, " Centro ", ServiceMode.ONLINE, pageable);

    verify(profiles).findPublished("Bruno", (short) 2, "Centro", ServiceMode.ONLINE, pageable);
  }
}
