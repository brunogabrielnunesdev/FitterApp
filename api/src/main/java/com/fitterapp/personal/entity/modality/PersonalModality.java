package com.fitterapp.personal.entity.modality;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fitterapp.personal.entity.profile.Profile;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "personal_modalities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalModality {

    @EmbeddedId
    private PersonalModalityId id;

    @MapsId("personalId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private Profile personal;

    @MapsId("modalityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modality_id", nullable = false)
    private Modality modality;


    public static PersonalModality link(Profile personal, Modality modality) {
        PersonalModality link = new PersonalModality();
        link.id = new PersonalModalityId(personal.getId(), modality.getId());
        link.personal = personal;
        link.modality = modality;
        return link;
    }



}
