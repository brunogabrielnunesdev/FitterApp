package com.fitterapp.user.repository;

import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.entity.UserRoleId;
import java.util.List;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

  @EntityGraph(attributePaths = {"role", "grantedBy"})
  List<UserRole> findAllByUserId(UUID userId);

  @EntityGraph(attributePaths = {"role", "grantedBy"})
  @Query(
      """
      select ur from UserRole ur
      where ur.user.id in :userIds
      order by ur.user.id, ur.role.id
      """)
  List<UserRole> findAllByUserIds(@Param("userIds") Collection<UUID> userIds);
}
