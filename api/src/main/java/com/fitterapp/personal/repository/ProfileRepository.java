package com.fitterapp.personal.repository;

import com.fitterapp.personal.entity.profile.Profile;
import com.fitterapp.personal.entity.profile.ProfileStatus;
import com.fitterapp.personal.entity.service.ServiceMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

  boolean existsByUserId(UUID userId);

  boolean existsBySlug(String slug);

  Optional<Profile> findByUserId(UUID userId);

  Optional<Profile> findByIdAndUserId(UUID id, UUID userId);

  List<Profile> findAllByStatusOrderByUpdatedAtAsc(ProfileStatus status);

  @EntityGraph(attributePaths = "publishedRevision")
  @Query(
      """
            select p from Profile p
            where p.status = com.fitterapp.personal.entity.profile.ProfileStatus.PUBLISHED
              and (:query is null or lower(p.publishedRevision.fullName) like lower(concat('%', :query, '%')))
              and (:modalityId is null or exists (select 1 from RevisionModality rm where rm.revision = p.publishedRevision and rm.modality.id = :modalityId))
              and (:neighborhood is null or exists (select 1 from RevisionServiceArea area where area.revision = p.publishedRevision and lower(area.neighborhood) = lower(:neighborhood)))
              and (:serviceMode is null or exists (select 1 from RevisionServiceMode mode where mode.revision = p.publishedRevision and mode.id.serviceMode = :serviceMode))
            order by lower(p.publishedRevision.fullName), p.id
            """)
  Page<Profile> findPublished(
      @Param("query") String query,
      @Param("modalityId") Short modalityId,
      @Param("neighborhood") String neighborhood,
      @Param("serviceMode") ServiceMode serviceMode,
      Pageable pageable);

  @EntityGraph(attributePaths = "publishedRevision")
  @Query(
      """
            select p from Profile p
            where p.status = com.fitterapp.personal.entity.profile.ProfileStatus.PUBLISHED
              and p.slug = :slug
            """)
  Optional<Profile> findPublishedBySlug(@Param("slug") String slug);
}
