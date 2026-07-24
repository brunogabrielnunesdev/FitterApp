package com.fitterapp.personal.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "personal_revision_service_modes")
public class PersonalRevisionServiceMode {

    @EmbeddedId
    private PersonalRevisionServiceModeId id;

    @MapsId("revisionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private PersonalProfileRevision revision;

    protected PersonalRevisionServiceMode() {
    }

    public static PersonalRevisionServiceMode of(
            PersonalProfileRevision revision,
            ServiceMode serviceMode) {
        PersonalRevisionServiceMode mode = new PersonalRevisionServiceMode();
        mode.id = new PersonalRevisionServiceModeId(revision.getId(), serviceMode);
        mode.revision = revision;
        return mode;
    }

    public PersonalRevisionServiceModeId getId() {
        return id;
    }

    public PersonalProfileRevision getRevision() {
        return revision;
    }

    public ServiceMode getServiceMode() {
        return id.getServiceMode();
    }
}
