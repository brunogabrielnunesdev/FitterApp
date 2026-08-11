package com.fitterapp.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitterapp.user.entity.Role;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.service.query.AdminUserDetails;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AdminUserMapperTests {

  @Test
  void mapsRolesWithoutExposingCredentialsOrInternalTokens() throws Exception {
    OffsetDateTime now = OffsetDateTime.parse("2026-08-11T16:00:00Z");
    UUID userId = UUID.randomUUID();
    User user =
        User.pendingRegistration(
            "Bruno", "bruno@example.com", "+5544999999999", "private-password-hash", now);
    user.confirmEmail(now.plusMinutes(1));
    ReflectionTestUtils.setField(user, "id", userId);
    Role role = mock(Role.class);
    when(role.getId()).thenReturn((short) 2);
    when(role.getName()).thenReturn(RoleName.PERSONAL);
    UserRole userRole = mock(UserRole.class);
    when(userRole.getRole()).thenReturn(role);
    when(userRole.getGrantedAt()).thenReturn(now.plusMinutes(2));

    var response = new AdminUserMapper().toDetail(new AdminUserDetails(user, List.of(userRole)));
    String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(response);

    assertThat(response.userId()).isEqualTo(userId);
    assertThat(response.roles()).singleElement().extracting(item -> item.name())
        .isEqualTo(RoleName.PERSONAL);
    assertThat(json)
        .doesNotContain("password", "hash", "token", "private-password-hash")
        .contains("bruno@example.com", "PERSONAL");
  }
}
