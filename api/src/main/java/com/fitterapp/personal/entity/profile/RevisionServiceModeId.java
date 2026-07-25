package com.fitterapp.personal.entity.profile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.fitterapp.personal.entity.service.ServiceMode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevisionServiceModeId implements Serializable {

    @Column(name = "revision_id")
    private UUID revisionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode", length = 30)
    private ServiceMode serviceMode;


    public RevisionServiceModeId(UUID revisionId, ServiceMode serviceMode) {
        this.revisionId = Objects.requireNonNull(revisionId);
        this.serviceMode = Objects.requireNonNull(serviceMode);
    }



    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RevisionServiceModeId that)) {
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
