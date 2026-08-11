package com.fitterapp.user.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ListAdminUsersServiceTests {
  @Mock private UserRepository users;
  @Mock private UserRoleRepository userRoles;

  @Test
  void normalizesSearchAndLoadsRolesInOneBatch() {
    UUID firstId = UUID.randomUUID();
    UUID secondId = UUID.randomUUID();
    User first = user(firstId);
    User second = user(secondId);
    UserRole firstRole = userRole(first);
    UserRole secondRole = userRole(second);
    var pageable = PageRequest.of(0, 20);
    when(users.findAllForAdministration(
            "%bruno%", UserStatus.ACTIVE, RoleName.PERSONAL, pageable))
        .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));
    when(userRoles.findAllByUserIds(List.of(firstId, secondId)))
        .thenReturn(List.of(firstRole, secondRole));

    var result =
        new ListAdminUsersService(users, userRoles)
            .list("  BRUNO  ", UserStatus.ACTIVE, RoleName.PERSONAL, pageable);

    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent().get(0).roles()).containsExactly(firstRole);
    assertThat(result.getContent().get(1).roles()).containsExactly(secondRole);
    verify(userRoles).findAllByUserIds(List.of(firstId, secondId));
  }

  @Test
  void blankSearchBecomesNullAndEmptyPageSkipsRoleQuery() {
    var pageable = PageRequest.of(0, 20);
    when(users.findAllForAdministration(null, null, null, pageable))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    var result = new ListAdminUsersService(users, userRoles).list(" ", null, null, pageable);

    assertThat(result).isEmpty();
    verify(users).findAllForAdministration(null, null, null, pageable);
  }

  private User user(UUID id) {
    User user = mock(User.class);
    when(user.getId()).thenReturn(id);
    return user;
  }

  private UserRole userRole(User user) {
    UserRole role = mock(UserRole.class);
    when(role.getUser()).thenReturn(user);
    return role;
  }
}
