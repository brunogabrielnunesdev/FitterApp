package com.fitterapp.user.service;

import com.fitterapp.user.entity.*;
import com.fitterapp.user.repository.*;
import jakarta.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserQueryService {
  private final UserRepository users;
  private final UserRoleRepository roles;

  public UserPage list(String query, UserStatus status, RoleName role, int page, int size) {
    var result =
        users.findAll(
            (root, criteria, cb) -> {
              List<Predicate> filters = new ArrayList<>();
              if (query != null && !query.isBlank()) {
                String term = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                filters.add(
                    cb.or(
                        cb.like(cb.lower(root.get("fullName")), term),
                        cb.like(cb.lower(root.get("email")), term)));
              }
              if (status != null) filters.add(cb.equal(root.get("status"), status));
              if (role != null) {
                var sub = criteria.subquery(UUID.class);
                var membership = sub.from(UserRole.class);
                sub.select(membership.get("user").get("id"))
                    .where(cb.equal(membership.get("role").get("name"), role));
                filters.add(root.get("id").in(sub));
              }
              return cb.and(filters.toArray(Predicate[]::new));
            },
            PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by("fullName").ascending().and(Sort.by("id"))));
    return new UserPage(
        result.getContent().stream().map(this::view).toList(),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages());
  }

  public UserView get(UUID id) {
    return view(
        users
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")));
  }

  private UserView view(User user) {
    var memberships =
        roles.findAllByUserId(user.getId()).stream()
            .map(
                r ->
                    new RoleView(
                        r.getRole().getName(),
                        r.getGrantedAt(),
                        r.getGrantedBy() == null ? null : r.getGrantedBy().getId()))
            .toList();
    return new UserView(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getPhoneNumber(),
        user.getStatus(),
        memberships,
        user.getCreatedAt(),
        user.getUpdatedAt(),
        user.getEmailVerifiedAt());
  }

  public record RoleView(RoleName name, OffsetDateTime grantedAt, UUID grantedByUserId) {}

  public record UserView(
      UUID userId,
      String fullName,
      String email,
      String phoneNumber,
      UserStatus status,
      List<RoleView> roles,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt,
      OffsetDateTime emailVerifiedAt) {}

  public record UserPage(
      List<UserView> content, int page, int size, long totalElements, int totalPages) {}
}
