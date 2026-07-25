package com.fitterapp.personal.entity.profile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevisionModalityId implements Serializable {

    @Column(name = "revision_id")
    private UUID revisionId;

    @Column(name = "modality_id")
    private Short modalityId;


    public RevisionModalityId(UUID revisionId, Short modalityId) {
        this.revisionId = Objects.requireNonNull(revisionId);
        this.modalityId = Objects.requireNonNull(modalityId);
    }



    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RevisionModalityId that)) {
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
