package com.fitterapp.personal.entity.service;

import com.fitterapp.personal.entity.profile.Profile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "personal_service_areas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalServiceArea {

  @Id @UuidGenerator private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "personal_id", nullable = false)
  private Profile personal;

  @Column(nullable = false, length = 100)
  private String city;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "state_code", nullable = false, length = 2, columnDefinition = "char(2)")
  private String stateCode;

  @Column(length = 100)
  private String neighborhood;

  @Column(length = 200)
  private String description;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  public static PersonalServiceArea create(
      Profile personal,
      String city,
      String stateCode,
      String neighborhood,
      String description,
      OffsetDateTime createdAt) {
    PersonalServiceArea area = new PersonalServiceArea();
    area.personal = personal;
    area.city = city;
    area.stateCode = stateCode;
    area.neighborhood = neighborhood;
    area.description = description;
    area.createdAt = createdAt;
    return area;
  }
}
