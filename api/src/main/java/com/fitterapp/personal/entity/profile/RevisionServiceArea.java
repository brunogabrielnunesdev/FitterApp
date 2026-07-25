package com.fitterapp.personal.entity.profile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fitterapp.personal.entity.profile.ProfileRevision;
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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevisionServiceArea {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private ProfileRevision revision;

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


    public static RevisionServiceArea create(
            ProfileRevision revision,
            String city,
            String stateCode,
            String neighborhood,
            String description,
            OffsetDateTime createdAt) {
        RevisionServiceArea area = new RevisionServiceArea();
        area.revision = revision;
        area.city = city;
        area.stateCode = stateCode;
        area.neighborhood = neighborhood;
        area.description = description;
        area.createdAt = createdAt;
        return area;
    }







}
