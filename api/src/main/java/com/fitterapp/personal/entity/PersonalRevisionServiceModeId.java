package com.fitterapp.personal.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class PersonalRevisionServiceModeId implements Serializable {

    @Column(name = "revision_id")
    private UUID revisionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode", length = 30)
    private ServiceMode serviceMode;

    protected PersonalRevisionServiceModeId() {
    }

    public PersonalRevisionServiceModeId(UUID revisionId, ServiceMode serviceMode) {
        this.revisionId = Objects.requireNonNull(revisionId);
        this.serviceMode = Objects.requireNonNull(serviceMode);
    }

    public UUID getRevisionId() {
        return revisionId;
    }

    public ServiceMode getServiceMode() {
        return serviceMode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PersonalRevisionServiceModeId that)) {
            return false;
        }
        return Objects.equals(revisionId, that.revisionId)
                && serviceMode == that.serviceMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(revisionId, serviceMode);
    }
}
