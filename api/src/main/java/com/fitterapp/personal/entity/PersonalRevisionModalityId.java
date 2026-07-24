package com.fitterapp.personal.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PersonalRevisionModalityId implements Serializable {

    @Column(name = "revision_id")
    private UUID revisionId;

    @Column(name = "modality_id")
    private Short modalityId;

    protected PersonalRevisionModalityId() {
    }

    public PersonalRevisionModalityId(UUID revisionId, Short modalityId) {
        this.revisionId = Objects.requireNonNull(revisionId);
        this.modalityId = Objects.requireNonNull(modalityId);
    }

    public UUID getRevisionId() {
        return revisionId;
    }

    public Short getModalityId() {
        return modalityId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PersonalRevisionModalityId that)) {
            return false;
        }
        return Objects.equals(revisionId, that.revisionId)
                && Objects.equals(modalityId, that.modalityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(revisionId, modalityId);
    }
}
