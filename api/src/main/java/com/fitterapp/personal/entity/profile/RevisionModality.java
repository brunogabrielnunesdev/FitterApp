package com.fitterapp.personal.entity.profile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "personal_revision_modalities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevisionModality {

    @EmbeddedId
    private RevisionModalityId id;

    @MapsId("revisionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private ProfileRevision revision;

    @MapsId("modalityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modality_id", nullable = false)
    private Modality modality;


    public static RevisionModality link(
            ProfileRevision revision,
            Modality modality) {
        RevisionModality link = new RevisionModality();
        link.id = new RevisionModalityId(revision.getId(), modality.getId());
        link.revision = revision;
        link.modality = modality;
        return link;
    }



}
