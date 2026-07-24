package com.fitterapp.personal.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "personal_modalities")
public class PersonalModality {

    @EmbeddedId
    private PersonalModalityId id;

    @MapsId("personalId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private PersonalProfile personal;

    @MapsId("modalityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modality_id", nullable = false)
    private Modality modality;

    protected PersonalModality() {
    }

    public static PersonalModality link(PersonalProfile personal, Modality modality) {
        PersonalModality link = new PersonalModality();
        link.id = new PersonalModalityId(personal.getId(), modality.getId());
        link.personal = personal;
        link.modality = modality;
        return link;
    }

    public PersonalModalityId getId() {
        return id;
    }

    public PersonalProfile getPersonal() {
        return personal;
    }

    public Modality getModality() {
        return modality;
    }
}
