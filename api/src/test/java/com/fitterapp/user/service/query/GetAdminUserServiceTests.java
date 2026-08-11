package com.fitterapp.user.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.exception.UserNotFoundException;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAdminUserServiceTests {
  @Mock private UserRepository users;
  @Mock private UserRoleRepository userRoles;

  @Test
  void returnsUserWithRoles() {
    UUID userId = UUID.randomUUID();
    User user = mock(User.class);
    UserRole role = mock(UserRole.class);
    when(users.findById(userId)).thenReturn(Optional.of(user));
    when(userRoles.findAllByUserId(userId)).thenReturn(List.of(role));

    var result = new GetAdminUserService(users, userRoles).get(userId);

    assertThat(result.user()).isSameAs(user);
    assertThat(result.roles()).containsExactly(role);
  }

  @Test
  void rejectsUnknownUser() {
    UUID userId = UUID.randomUUID();
    when(users.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> new GetAdminUserService(users, userRoles).get(userId))
        .isInstanceOf(UserNotFoundException.class);
  }
}
