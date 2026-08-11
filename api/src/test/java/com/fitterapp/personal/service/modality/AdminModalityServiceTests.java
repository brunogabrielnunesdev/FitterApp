package com.fitterapp.personal.service.modality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.exception.DuplicateModalityException;
import com.fitterapp.personal.repository.ModalityRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminModalityServiceTests {
  private final ModalityRepository modalities = mock(ModalityRepository.class);
  private final Clock clock = Clock.fixed(Instant.parse("2026-08-11T18:00:00Z"), ZoneOffset.UTC);
  private final ModalitySlugGenerator slugGenerator = new ModalitySlugGenerator();
  private AdminModalityService service;

  @BeforeEach
  void setUp() {
    service = new AdminModalityService(modalities, slugGenerator, clock);
  }

  @Test
  void createsActiveModalityWithNormalizedNameAndSlug() {
    when(modalities.saveAndFlush(any(Modality.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Modality created = service.create("  Pilates   Clínico  ");

    assertThat(created.getName()).isEqualTo("Pilates Clínico");
    assertThat(created.getSlug()).isEqualTo("pilates-clinico");
    assertThat(created.isActive()).isTrue();
    assertThat(created.getCreatedAt()).isEqualTo(Instant.now(clock).atOffset(ZoneOffset.UTC));
  }

  @Test
  void rejectsCaseInsensitiveDuplicateName() {
    when(modalities.existsByNameIgnoreCase("pilates")).thenReturn(true);

    assertThatThrownBy(() -> service.create("pilates"))
        .isInstanceOf(DuplicateModalityException.class);
  }

  @Test
  void deactivatesWithoutDeletingTheModality() {
    Modality modality = mock(Modality.class);
    when(modalities.findById((short) 9)).thenReturn(Optional.of(modality));

    assertThat(service.setActive((short) 9, false)).isSameAs(modality);

    verify(modality).setActive(false, Instant.now(clock).atOffset(ZoneOffset.UTC));
  }
}
