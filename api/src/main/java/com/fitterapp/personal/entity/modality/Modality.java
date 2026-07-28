package com.fitterapp.personal.entity.modality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "modalities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Modality {

  @Id private Short id;

  @Column(nullable = false, unique = true, length = 80)
  private String name;

  @Column(nullable = false, unique = true, length = 80)
  private String slug;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
