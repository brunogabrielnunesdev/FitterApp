package com.fitterapp.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fitterapp.user.dto.admin.AdminUserDetailDto;
import com.fitterapp.user.dto.admin.AdminUserPageDto;
import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.mapper.AdminUserMapper;
import com.fitterapp.user.service.query.AdminUserDetails;
import com.fitterapp.user.service.query.GetAdminUserService;
import com.fitterapp.user.service.query.ListAdminUsersService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTests {
  @Mock private ListAdminUsersService listUsers;
  @Mock private GetAdminUserService getUser;
  @Mock private AdminUserMapper mapper;

  @Test
  void listsWithFiltersSafePaginationAndStableSorting() {
    var mapped = new AdminUserPageDto(List.of(), 0, 100, 0, 0);
    when(listUsers.list(
            eq("bruno"), eq(UserStatus.ACTIVE), eq(RoleName.PERSONAL), any()))
        .thenReturn(Page.empty());
    when(mapper.toPage(any())).thenReturn(mapped);

    var response =
        controller().list("bruno", UserStatus.ACTIVE, RoleName.PERSONAL, -2, 500);

    assertThat(response.getBody()).isSameAs(mapped);
    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(listUsers)
        .list(eq("bruno"), eq(UserStatus.ACTIVE), eq(RoleName.PERSONAL), pageable.capture());
    assertThat(pageable.getValue().getPageNumber()).isZero();
    assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
    assertThat(pageable.getValue().getSort().getOrderFor("createdAt").isDescending()).isTrue();
    assertThat(pageable.getValue().getSort().getOrderFor("id").isAscending()).isTrue();
  }

  @Test
  void getsUserDetail() {
    UUID userId = UUID.randomUUID();
    AdminUserDetails details = mock(AdminUserDetails.class);
    AdminUserDetailDto mapped = mock(AdminUserDetailDto.class);
    when(getUser.get(userId)).thenReturn(details);
    when(mapper.toDetail(details)).thenReturn(mapped);

    assertThat(controller().get(userId).getBody()).isSameAs(mapped);
  }

  private AdminUserController controller() {
    return new AdminUserController(listUsers, getUser, mapper);
  }
}
