package com.fitterapp.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.analytics.controller.AdminDashboardController;
import com.fitterapp.personal.controller.AdminModalityController;
import com.fitterapp.personal.controller.AdminPersonalManagementController;
import com.fitterapp.personal.controller.AdminProfileController;
import com.fitterapp.user.controller.AdminUserController;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;

class AuthorizationConventionTests {
  private static final List<Class<?>> ADMIN_CONTROLLERS =
      List.of(
          AdminDashboardController.class,
          AdminModalityController.class,
          AdminPersonalManagementController.class,
          AdminProfileController.class,
          AdminUserController.class);

  @Test
  void keepsEveryAdministrativeControllerUnderTheAdminPathAndRoleGuard() {
    for (Class<?> controller : ADMIN_CONTROLLERS) {
      RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
      PreAuthorize authorization = controller.getAnnotation(PreAuthorize.class);

      assertThat(mapping)
          .as("%s must declare a request mapping", controller.getSimpleName())
          .isNotNull();
      assertThat(mapping.value())
          .as("%s must stay under /api/v1/admin", controller.getSimpleName())
          .allMatch(path -> path.startsWith("/api/v1/admin/"));
      assertThat(authorization)
          .as("%s must have a method-level role guard", controller.getSimpleName())
          .isNotNull();
      assertThat(authorization.value()).isEqualTo("hasAnyRole('ADMIN', 'OWNER')");
    }
  }
}
