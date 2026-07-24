package com.fitterapp.personal.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "personal_service_modes")
public class PersonalServiceMode {

    @EmbeddedId
    private PersonalServiceModeId id;

    @MapsId("personalId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private PersonalProfile personal;

    protected PersonalServiceMode() {
    }

    public static PersonalServiceMode of(PersonalProfile personal, ServiceMode serviceMode) {
        PersonalServiceMode mode = new PersonalServiceMode();
        mode.id = new PersonalServiceModeId(personal.getId(), serviceMode);
        mode.personal = personal;
        return mode;
    }

    public PersonalServiceModeId getId() {
        return id;
    }

    public PersonalProfile getPersonal() {
        return personal;
    }

    public ServiceMode getServiceMode() {
        return id.getServiceMode();
    }
}
