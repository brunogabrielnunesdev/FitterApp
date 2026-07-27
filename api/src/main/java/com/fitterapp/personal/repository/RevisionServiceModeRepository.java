package com.fitterapp.personal.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fitterapp.personal.entity.profile.RevisionServiceMode;
import com.fitterapp.personal.entity.profile.RevisionServiceModeId;

public interface RevisionServiceModeRepository
        extends JpaRepository<RevisionServiceMode, RevisionServiceModeId> {

    void deleteByRevisionId(UUID revisionId);

    long countByIdRevisionId(UUID revisionId);
}
