package com.fitterapp.user.mapper;

import com.fitterapp.user.dto.admin.AdminUserDetailDto;
import com.fitterapp.user.dto.admin.AdminUserPageDto;
import com.fitterapp.user.dto.admin.AdminUserRoleDto;
import com.fitterapp.user.dto.admin.AdminUserSummaryDto;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.service.query.AdminUserDetails;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class AdminUserMapper {

  public AdminUserPageDto toPage(Page<AdminUserDetails> page) {
    return new AdminUserPageDto(
        page.getContent().stream().map(this::toSummary).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  public AdminUserDetailDto toDetail(AdminUserDetails details) {
    var user = details.user();
    return new AdminUserDetailDto(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getPhoneNumber(),
        user.getStatus(),
        user.getEmailVerifiedAt(),
        toRoles(details.roles()),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  private AdminUserSummaryDto toSummary(AdminUserDetails details) {
    var user = details.user();
    return new AdminUserSummaryDto(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getPhoneNumber(),
        user.getStatus(),
        toRoles(details.roles()),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  private List<AdminUserRoleDto> toRoles(List<UserRole> roles) {
    return roles.stream()
        .sorted(Comparator.comparing(userRole -> userRole.getRole().getId()))
        .map(
            userRole ->
                new AdminUserRoleDto(
                    userRole.getRole().getName(),
                    userRole.getGrantedAt(),
                    userRole.getGrantedBy() == null ? null : userRole.getGrantedBy().getId()))
        .toList();
  }
}
