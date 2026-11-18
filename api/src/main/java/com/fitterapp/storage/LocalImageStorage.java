package com.fitterapp.storage;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LocalImageStorage implements ImageStorage {
  private static final long MAX_BYTES = 5L * 1024 * 1024;
  private static final long MAX_PIXELS = 24_000_000L;
  private static final int MAX_EDGE = 1600;
  private final Path root;

  public LocalImageStorage(@Value("${fitterapp.storage.root:./uploads}") String root) {
    this.root = Path.of(root).toAbsolutePath().normalize();
  }

  @Override
  public String store(MultipartFile file, String namespace, UUID profileId) {
    if (!List.of("personals", "private/crefs").contains(namespace))
      throw new IllegalArgumentException("Invalid namespace");
    if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES)
      throw invalid("A imagem deve ter no máximo 5 MB");
    byte[] bytes;
    try {
      bytes = file.getBytes();
    } catch (IOException e) {
      throw invalid("Não foi possível ler a imagem");
    }
    if (bytes.length > MAX_BYTES || !supported(bytes))
      throw invalid("Envie uma imagem JPEG, PNG ou WebP válida");
    BufferedImage image;
    try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      if (input == null) throw invalid("Imagem inválida");
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) throw invalid("Imagem inválida");
      var reader = readers.next();
      try {
        reader.setInput(input, true, true);
        int width = reader.getWidth(0), height = reader.getHeight(0);
        if (width <= 0 || height <= 0 || (long) width * height > MAX_PIXELS)
          throw invalid("Dimensões de imagem não suportadas");
        image = reader.read(0);
        if (image == null) throw invalid("Imagem inválida");
      } finally {
        reader.dispose();
      }
    } catch (IOException | IndexOutOfBoundsException e) {
      throw invalid("Imagem inválida");
    }
    double scale = Math.min(1d, (double) MAX_EDGE / Math.max(image.getWidth(), image.getHeight()));
    int width = Math.max(1, (int) Math.round(image.getWidth() * scale)),
        height = Math.max(1, (int) Math.round(image.getHeight() * scale));
    BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = resized.createGraphics();
    try {
      graphics.setRenderingHint(
          RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics.drawImage(image, 0, 0, width, height, null);
    } finally {
      graphics.dispose();
    }
    String key = namespace + "/" + profileId + "/" + UUID.randomUUID() + ".webp";
    Path target = resolve(key);
    try {
      Files.createDirectories(target.getParent());
      try (OutputStream output =
          Files.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
        if (!ImageIO.write(resized, "webp", output))
          throw new IOException("WebP writer unavailable");
      }
      return key;
    } catch (IOException e) {
      try {
        Files.deleteIfExists(target);
      } catch (IOException ignored) {
      }
      throw new IllegalStateException("Não foi possível armazenar a imagem", e);
    }
  }

  @Override
  public byte[] read(String key) {
    try {
      return Files.readAllBytes(resolve(key));
    } catch (NoSuchFileException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada");
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível ler a imagem", e);
    }
  }

  @Override
  public void delete(String key) {
    try {
      Files.deleteIfExists(resolve(key));
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível remover a imagem", e);
    }
  }

  private Path resolve(String key) {
    if (key == null
        || !key.matches("(?:personals|private/crefs)/[0-9a-f-]{36}/[0-9a-f-]{36}\\.webp"))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada");
    Path result = root.resolve(key).normalize();
    if (!result.startsWith(root))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada");
    return result;
  }

  private boolean supported(byte[] b) {
    return (b.length >= 3 && (b[0] & 255) == 0xff && (b[1] & 255) == 0xd8 && (b[2] & 255) == 0xff)
        || (b.length >= 8
            && (b[0] & 255) == 0x89
            && b[1] == 'P'
            && b[2] == 'N'
            && b[3] == 'G'
            && b[4] == 13
            && b[5] == 10
            && b[6] == 26
            && b[7] == 10)
        || (b.length >= 12
            && b[0] == 'R'
            && b[1] == 'I'
            && b[2] == 'F'
            && b[3] == 'F'
            && b[8] == 'W'
            && b[9] == 'E'
            && b[10] == 'B'
            && b[11] == 'P');
  }

  private ResponseStatusException invalid(String message) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }
}
