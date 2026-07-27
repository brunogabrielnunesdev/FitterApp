package com.fitterapp.personal.dto.profile;
import java.util.UUID; import com.fitterapp.personal.entity.profile.ProfileStatus; import com.fitterapp.personal.entity.profile.ProfileRevisionStatus;
public record ProfileStatusResponseDto(UUID profileId,String fullName,ProfileStatus profileStatus,ProfileRevisionStatus revisionStatus,String rejectionReason) { }
