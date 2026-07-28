package com.fitterapp.personal.entity.profile;

import com.fitterapp.personal.entity.service.ServiceMode;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personal_revision_service_modes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevisionServiceMode {

  @EmbeddedId private RevisionServiceModeId id;

  @MapsId("revisionId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "revision_id", nullable = false)
  private ProfileRevision revision;

  public static RevisionServiceMode of(ProfileRevision revision, ServiceMode serviceMode) {
    RevisionServiceMode mode = new RevisionServiceMode();
    mode.id = new RevisionServiceModeId(revision.getId(), serviceMode);
    mode.revision = revision;
    return mode;
  }

  public ServiceMode getServiceMode() {
    return id.getServiceMode();
  }
}
