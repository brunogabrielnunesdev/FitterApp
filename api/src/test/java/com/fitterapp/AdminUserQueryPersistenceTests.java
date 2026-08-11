package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.user.entity.RoleName;
import com.fitterapp.user.entity.User;
import com.fitterapp.user.entity.UserRole;
import com.fitterapp.user.entity.UserStatus;
import com.fitterapp.user.repository.RoleRepository;
import com.fitterapp.user.repository.UserRepository;
import com.fitterapp.user.repository.UserRoleRepository;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminUserQueryPersistenceTests {
  @Autowired private EntityManager entityManager;
  @Autowired private UserRepository users;
  @Autowired private UserRoleRepository userRoles;
  @Autowired private RoleRepository roles;

  @Test
  void searchesAndFiltersUsersByStatusAndRole() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC).withNano(0);
    User bruno = activeUser("Bruno Personal", "bruno.personal@example.com", "+5544999999910", now);
    User ana =
        User.pendingRegistration(
            "Ana Student", "ana.student@example.com", "+5544999999911", "hash", now);
    entityManager.persist(ana);
    entityManager.flush();
    var personalRole = roles.findByName(RoleName.PERSONAL).orElseThrow();
    var studentRole = roles.findByName(RoleName.STUDENT).orElseThrow();
    userRoles.save(UserRole.grantedBySystem(bruno, personalRole, now));
    userRoles.save(UserRole.grantedBySystem(ana, studentRole, now));
    entityManager.flush();
    entityManager.clear();

    var byNameAndRole =
        users.findAllForAdministration(
            "%bruno%", UserStatus.ACTIVE, RoleName.PERSONAL, PageRequest.of(0, 10));
    var byEmail =
        users.findAllForAdministration(
            "%ana.student@%", UserStatus.PENDING_VERIFICATION, null, PageRequest.of(0, 10));
    var wrongRole =
        users.findAllForAdministration(
            "%bruno%", UserStatus.ACTIVE, RoleName.STUDENT, PageRequest.of(0, 10));

    assertThat(byNameAndRole.getContent()).singleElement().extracting(User::getId)
        .isEqualTo(bruno.getId());
    assertThat(byEmail.getContent()).singleElement().extracting(User::getId).isEqualTo(ana.getId());
    assertThat(wrongRole).isEmpty();

    var loadedRoles = userRoles.findAllByUserIds(List.of(bruno.getId(), ana.getId()));
    assertThat(loadedRoles).hasSize(2).allSatisfy(link -> assertThat(Hibernate.isInitialized(link.getRole())).isTrue());
  }

  private User activeUser(
      String fullName, String email, String phoneNumber, OffsetDateTime createdAt) {
    User user = User.pendingRegistration(fullName, email, phoneNumber, "hash", createdAt);
    user.confirmEmail(createdAt);
    entityManager.persist(user);
    entityManager.flush();
    return user;
  }
}
