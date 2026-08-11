package com.fitterapp.personal.entity.modality;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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

  public static Modality create(
      String name, String slug, boolean active, OffsetDateTime createdAt) {
    Modality modality = new Modality();
    modality.name = name;
    modality.slug = slug;
    modality.active = active;
    modality.createdAt = createdAt;
    modality.updatedAt = createdAt;
    return modality;
  }

  public void rename(String name, String slug, OffsetDateTime updatedAt) {
    this.name = name;
    this.slug = slug;
    this.updatedAt = updatedAt;
  }

  public void setActive(boolean active, OffsetDateTime updatedAt) {
    this.active = active;
    this.updatedAt = updatedAt;
  }
}
