package com.fitterapp.personal.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "personal_revision_modalities")
public class PersonalRevisionModality {

    @EmbeddedId
    private PersonalRevisionModalityId id;

    @MapsId("revisionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private PersonalProfileRevision revision;

    @MapsId("modalityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modality_id", nullable = false)
    private Modality modality;

    protected PersonalRevisionModality() {
    }

    public static PersonalRevisionModality link(
            PersonalProfileRevision revision,
            Modality modality) {
        PersonalRevisionModality link = new PersonalRevisionModality();
        link.id = new PersonalRevisionModalityId(revision.getId(), modality.getId());
        link.revision = revision;
        link.modality = modality;
        return link;
    }

    public PersonalRevisionModalityId getId() {
        return id;
    }

    public PersonalProfileRevision getRevision() {
        return revision;
    }

    public Modality getModality() {
        return modality;
    }
}
