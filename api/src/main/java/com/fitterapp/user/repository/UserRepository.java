package com.fitterapp.user.repository;

import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.UserStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);

  @Query(
      """
      select u from User u
      where (:searchPattern is null
          or lower(u.fullName) like :searchPattern
          or lower(u.email) like :searchPattern)
        and (:status is null or u.status = :status)
        and (:role is null or exists (
          select ur.id from UserRole ur
          where ur.user = u and ur.role.name = :role
        ))
      """)
  Page<User> findAllForAdministration(
      @Param("searchPattern") String searchPattern,
      @Param("status") UserStatus status,
      @Param("role") RoleName role,
      Pageable pageable);
}
