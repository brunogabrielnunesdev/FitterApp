package com.fitterapp.personal.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "personal_revision_service_areas")
public class PersonalRevisionServiceArea {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private PersonalProfileRevision revision;

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

    protected PersonalRevisionServiceArea() {
    }

    public static PersonalRevisionServiceArea create(
            PersonalProfileRevision revision,
            String city,
            String stateCode,
            String neighborhood,
            String description,
            OffsetDateTime createdAt) {
        PersonalRevisionServiceArea area = new PersonalRevisionServiceArea();
        area.revision = revision;
        area.city = city;
        area.stateCode = stateCode;
        area.neighborhood = neighborhood;
        area.description = description;
        area.createdAt = createdAt;
        return area;
    }

    public UUID getId() {
        return id;
    }

    public PersonalProfileRevision getRevision() {
        return revision;
    }

    public String getCity() {
        return city;
    }

    public String getStateCode() {
        return stateCode;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
