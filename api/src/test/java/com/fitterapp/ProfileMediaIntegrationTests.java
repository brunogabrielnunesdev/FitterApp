package com.fitterapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitterapp.personal.entity.profile.*;
import com.fitterapp.user.entity.User;
import jakarta.persistence.EntityManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ProfileMediaIntegrationTests {
  @TempDir static Path uploads;

  @DynamicPropertySource
  static void storage(DynamicPropertyRegistry registry) {
    registry.add("fitterapp.storage.root", () -> uploads.toString());
  }

  @Autowired MockMvc mvc;
  @Autowired EntityManager em;
  @Autowired ObjectMapper json;

  @Test
  void ownerUploadsAndOnlyPublishedPhotoIsPublicWhileCrefRequiresAdmin() throws Exception {
    var now = OffsetDateTime.now();
    var user =
        User.pendingRegistration("Ana", "media-test@example.com", "+5544999999999", "hash", now);
    user.confirmEmail(now);
    em.persist(user);
    em.flush();
    var profile = Profile.draft("Ana", "ana-media", now);
    profile.linkUser(user, now);
    em.persist(profile);
    em.flush();
    var revision = ProfileRevision.draft(profile, 1, user, true, now);
    em.persist(revision);
    em.flush();
    profile.setCurrentRevision(revision, now);
    em.flush();
    var bytes = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB), "png", bytes);
    var photo = new MockMultipartFile("file", "photo.png", "image/png", bytes.toByteArray());
    var result =
        mvc.perform(
                multipart("/api/v1/me/personal-profile/{id}/photo", profile.getId())
                    .file(photo)
                    .with(jwt().jwt(j -> j.subject(user.getId().toString()))))
            .andExpect(status().isOk())
            .andReturn();
    String key = json.readTree(result.getResponse().getContentAsString()).path("key").asText();
    UUID imageId = UUID.fromString(key.substring(key.lastIndexOf('/') + 1, key.length() - 5));
    mvc.perform(get("/api/v1/public/media/personals/{id}/{image}.webp", profile.getId(), imageId))
        .andExpect(status().isNotFound());
    mvc.perform(
            get("/api/v1/admin/personal-profiles/{id}/photo", profile.getId())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(content().contentType("image/webp"));
    var doc = new MockMultipartFile("file", "cref.png", "image/png", bytes.toByteArray());
    mvc.perform(
            multipart("/api/v1/me/personal-profile/{id}/cref/document", profile.getId())
                .file(doc)
                .param("registrationCode", "CREF-123")
                .with(jwt().jwt(j -> j.subject(user.getId().toString()))))
        .andExpect(status().isOk());
    mvc.perform(
            get("/api/v1/admin/personal-profiles/{id}/cref/document", profile.getId())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            get("/api/v1/admin/personal-profiles/{id}/cref/document", profile.getId())
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(content().contentType("image/webp"));
    profile.publish(revision, now);
    em.flush();
    var publicPhoto =
        mvc.perform(
                get("/api/v1/public/media/personals/{id}/{image}.webp", profile.getId(), imageId))
            .andExpect(status().isOk())
            .andExpect(content().contentType("image/webp"))
            .andReturn();
    assertThat(publicPhoto.getResponse().getContentAsByteArray())
        .startsWith((byte) 'R', (byte) 'I', (byte) 'F', (byte) 'F');
  }
}
