package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fitterapp.personal.exception.DuplicateModalityException;
import com.fitterapp.personal.service.modality.AdminModalityService;
import com.fitterapp.personal.service.modality.ListActiveModalitiesService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
class AdminModalityManagementPersistenceTests {
  @Autowired private AdminModalityService adminService;
  @Autowired private ListActiveModalitiesService publicService;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void managesModalityWithoutRemovingHistoricalProfileLinks() {
    var created = adminService.create("Pilates Clínico");
    assertThat(created.getId()).isGreaterThan((short) 8);
    assertThat(publicService.list())
        .extracting(modality -> modality.getId())
        .contains(created.getId());

    UUID profileId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    jdbcTemplate.update(
        """
        INSERT INTO personal_profiles (id, full_name, slug, status, created_at, updated_at)
        VALUES (?, ?, ?, 'DRAFT', ?, ?)
        """,
        profileId,
        "Historical Personal",
        "historical-personal",
        now,
        now);
    jdbcTemplate.update(
        "INSERT INTO personal_modalities (personal_id, modality_id) VALUES (?, ?)",
        profileId,
        created.getId());

    adminService.setActive(created.getId(), false);

    assertThat(publicService.list())
        .extracting(modality -> modality.getId())
        .doesNotContain(created.getId());
    assertThat(adminService.list())
        .filteredOn(modality -> modality.getId().equals(created.getId()))
        .singleElement()
        .satisfies(modality -> assertThat(modality.isActive()).isFalse());
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT count(*) FROM personal_modalities WHERE personal_id = ? AND modality_id = ?",
                Integer.class,
                profileId,
                created.getId()))
        .isEqualTo(1);
  }

  @Test
  void rejectsCaseInsensitiveDuplicateAndUpdatesNameAndSlug() {
    var created = adminService.create("Treino Híbrido");

    assertThatThrownBy(() -> adminService.create("treino híbrido"))
        .isInstanceOf(DuplicateModalityException.class);

    var updated = adminService.update(created.getId(), "Condicionamento Físico");
    assertThat(updated.getName()).isEqualTo("Condicionamento Físico");
    assertThat(updated.getSlug()).isEqualTo("condicionamento-fisico");
  }
}
