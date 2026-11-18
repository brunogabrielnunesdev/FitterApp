package com.fitterapp.personal.service.modality;

import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.repository.ModalityRepository;
import java.text.Normalizer;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminModalityService {
  private final ModalityRepository repository;
  private final Clock clock;

  @Transactional(readOnly = true)
  public List<View> list() {
    return repository.findAll().stream()
        .sorted(Comparator.comparing(Modality::getName, String.CASE_INSENSITIVE_ORDER))
        .map(this::view)
        .toList();
  }

  @Transactional
  public View create(String name) {
    String trimmed = name.trim();
    String slug = slug(trimmed);
    ensureUnique(trimmed, slug, null);
    var now = OffsetDateTime.now(clock);
    var m = new Modality(null, trimmed, slug, true, now, now);
    repository.saveAndFlush(m);
    return view(m);
  }

  @Transactional
  public View update(Short id, String name) {
    var m = get(id);
    String trimmed = name.trim();
    String slug = slug(trimmed);
    ensureUnique(trimmed, slug, id);
    m.setName(trimmed);
    m.setSlug(slug);
    m.setUpdatedAt(OffsetDateTime.now(clock));
    return view(m);
  }

  @Transactional
  public View setActive(Short id, boolean active) {
    var m = get(id);
    m.setActive(active);
    m.setUpdatedAt(OffsetDateTime.now(clock));
    return view(m);
  }

  private Modality get(Short id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modalidade não encontrada"));
  }

  private void ensureUnique(String name, String slug, Short id) {
    if (repository.findAll().stream()
        .anyMatch(
            m ->
                !Objects.equals(m.getId(), id)
                    && (m.getName().equalsIgnoreCase(name) || m.getSlug().equals(slug))))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Modalidade já cadastrada");
  }

  private String slug(String name) {
    String value =
        Normalizer.normalize(name, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-+|-+$)", "");
    if (value.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome inválido");
    return value;
  }

  private View view(Modality m) {
    return new View(m.getId(), m.getName(), m.getSlug(), m.isActive());
  }

  public record View(Short id, String name, String slug, boolean active) {}
}
