package com.fitterapp.personal.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class PersonalServiceModeId implements Serializable {

    @Column(name = "personal_id")
    private UUID personalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode", length = 30)
    private ServiceMode serviceMode;

    protected PersonalServiceModeId() {
    }

    public PersonalServiceModeId(UUID personalId, ServiceMode serviceMode) {
        this.personalId = Objects.requireNonNull(personalId);
        this.serviceMode = Objects.requireNonNull(serviceMode);
    }

    public UUID getPersonalId() {
        return personalId;
    }

    public ServiceMode getServiceMode() {
        return serviceMode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PersonalServiceModeId that)) {
            return false;
        }
        return Objects.equals(personalId, that.personalId)
                && serviceMode == that.serviceMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(personalId, serviceMode);
    }
}
