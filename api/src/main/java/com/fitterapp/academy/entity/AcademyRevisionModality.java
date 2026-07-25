package com.fitterapp.academy.entity;

import com.fitterapp.personal.entity.Modality;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "academy_revision_modalities")
public class AcademyRevisionModality {

    @EmbeddedId
    private AcademyRevisionModalityId id;

    @MapsId("revisionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private AcademyProfileRevision revision;

    @MapsId("modalityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modality_id", nullable = false)
    private Modality modality;

    protected AcademyRevisionModality() {
    }

    public static AcademyRevisionModality link(
            AcademyProfileRevision revision,
            Modality modality) {
        AcademyRevisionModality link = new AcademyRevisionModality();
        link.id = new AcademyRevisionModalityId(
                revision.getId(),
                modality.getId());
        link.revision = revision;
        link.modality = modality;
        return link;
    }

    public AcademyRevisionModalityId getId() {
        return id;
    }

    public AcademyProfileRevision getRevision() {
        return revision;
    }

    public Modality getModality() {
        return modality;
    }
}
