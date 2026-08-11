package com.fitterapp.personal.service.query;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileRevision;
import com.fitterapp.personal.entity.profile.RevisionModality;
import com.fitterapp.personal.entity.profile.RevisionServiceArea;
import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import java.util.List;

public record AdminProfileDetails(
    Profile profile,
    ProfileRevision revision,
    List<RevisionModality> modalities,
    List<RevisionServiceMode> serviceModes,
    List<RevisionServiceArea> serviceAreas) {}
