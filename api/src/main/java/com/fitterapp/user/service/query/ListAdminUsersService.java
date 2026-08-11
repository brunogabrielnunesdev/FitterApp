package com.fitterapp.user.service.query;

import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListAdminUsersService {
  private final UserRepository users;
  private final UserRoleRepository userRoles;

  @Transactional(readOnly = true)
  public Page<AdminUserDetails> list(
      String query, UserStatus status, RoleName role, Pageable pageable) {
    Page<com.fitterapp.user.entity.User> page =
        users.findAllForAdministration(searchPattern(query), status, role, pageable);
    if (page.isEmpty()) return page.map(user -> new AdminUserDetails(user, List.of()));

    List<UUID> userIds = page.getContent().stream().map(user -> user.getId()).toList();
    Map<UUID, List<UserRole>> rolesByUser =
        userRoles.findAllByUserIds(userIds).stream()
            .collect(Collectors.groupingBy(userRole -> userRole.getUser().getId()));
    return page.map(
        user -> new AdminUserDetails(user, rolesByUser.getOrDefault(user.getId(), List.of())));
  }

  private String searchPattern(String query) {
    if (query == null || query.isBlank()) return null;
    return "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
  }
}
