package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fitterapp.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class OwnAccountIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired EntityManager em;

  @Test
  void accountIsAuthenticatedAndOnlyUpdatesTheJwtOwner() throws Exception {
    var now = OffsetDateTime.now();
    var user =
        User.pendingRegistration("Ana Silva", "ana-own@example.com", "+5544999999999", "hash", now);
    user.confirmEmail(now);
    em.persist(user);
    var other =
        User.pendingRegistration(
            "Outro Usuário", "other-own@example.com", "+5544888888888", "hash", now);
    other.confirmEmail(now);
    em.persist(other);
    em.flush();
    mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/v1/me").with(jwt().jwt(j -> j.subject(user.getId().toString()))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("ana-own@example.com"));
    String body =
        """
      {"fullName":"  Ana   Souza ","phoneNumber":"+5544777777777","userId":"%s","email":"other@example.com"}
      """
            .formatted(other.getId());
    mvc.perform(
            put("/api/v1/me")
                .with(jwt().jwt(j -> j.subject(user.getId().toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fullName").value("Ana Souza"))
        .andExpect(jsonPath("$.email").value("ana-own@example.com"));
    em.flush();
    em.clear();
    assertThat(em.find(User.class, user.getId()).getPhoneNumber()).isEqualTo("+5544777777777");
    assertThat(em.find(User.class, other.getId()).getFullName()).isEqualTo("Outro Usuário");
    mvc.perform(
            put("/api/v1/me")
                .with(jwt().jwt(j -> j.subject(user.getId().toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Ana\",\"phoneNumber\":\"123\"}"))
        .andExpect(status().isBadRequest());
  }
}
