package com.fitterapp.storage;

import static org.assertj.core.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class LocalImageStorageTests {
  @TempDir Path directory;

  @Test
  void convertsAndResizesPngToWebp() throws Exception {
    var storage = new LocalImageStorage(directory.toString());
    var png = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB), "png", png);
    String key =
        storage.store(
            new MockMultipartFile("file", "photo.png", "image/png", png.toByteArray()),
            "personals",
            UUID.randomUUID());
    byte[] data = storage.read(key);
    assertThat(data).startsWith((byte) 'R', (byte) 'I', (byte) 'F', (byte) 'F');
    var converted = ImageIO.read(new ByteArrayInputStream(data));
    assertThat(converted.getWidth()).isEqualTo(1600);
    assertThat(converted.getHeight()).isEqualTo(800);
    storage.delete(key);
    assertThatThrownBy(() -> storage.read(key)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void rejectsSpoofedAndOversizedFiles() {
    var storage = new LocalImageStorage(directory.toString());
    var fake = new MockMultipartFile("file", "photo.png", "image/png", "<svg/>".getBytes());
    assertThatThrownBy(() -> storage.store(fake, "personals", UUID.randomUUID()))
        .isInstanceOf(ResponseStatusException.class);
    var large =
        new MockMultipartFile("file", "photo.png", "image/png", new byte[5 * 1024 * 1024 + 1]);
    assertThatThrownBy(() -> storage.store(large, "personals", UUID.randomUUID()))
        .isInstanceOf(ResponseStatusException.class);
    assertThatThrownBy(() -> storage.read("../../private/file.webp"))
        .isInstanceOf(ResponseStatusException.class);
  }
}
