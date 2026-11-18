package com.fitterapp.personal.entity.modality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "modalities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Modality {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modality_id_generator")
  @SequenceGenerator(
      name = "modality_id_generator",
      sequenceName = "modalities_id_seq",
      allocationSize = 1)
  private Short id;

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
