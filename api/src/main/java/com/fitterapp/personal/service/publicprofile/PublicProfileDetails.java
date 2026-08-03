package com.fitterapp.personal.service.publicprofile;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import java.util.List;

public record PublicProfileDetails(
    Profile profile,
    ProfileRevision revision,
    List<RevisionModality> modalities,
    List<RevisionServiceMode> serviceModes,
    List<RevisionServiceArea> serviceAreas) {}
