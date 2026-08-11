package com.fitterapp.personal.service.modality;

import com.fitterapp.personal.entity.modality.Modality;
import com.fitterapp.personal.exception.DuplicateModalityException;
import com.fitterapp.personal.exception.ModalityNotFoundException;
import com.fitterapp.personal.repository.ModalityRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminModalityService {
  private final ModalityRepository modalities;
  private final ModalitySlugGenerator slugGenerator;
  private final Clock clock;

  @Transactional(readOnly = true)
  public List<Modality> list() {
    return modalities.findAllByOrderByNameAsc();
  }

  @Transactional
  public Modality create(String requestedName) {
    String name = normalizeName(requestedName);
    String slug = slugGenerator.generate(name);
    ensureAvailable(name, slug, null);
    try {
      return modalities.saveAndFlush(Modality.create(name, slug, true, OffsetDateTime.now(clock)));
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateModalityException();
    }
  }

  @Transactional
  public Modality update(Short id, String requestedName) {
    Modality modality = find(id);
    String name = normalizeName(requestedName);
    String slug = slugGenerator.generate(name);
    ensureAvailable(name, slug, id);
    modality.rename(name, slug, OffsetDateTime.now(clock));
    try {
      return modalities.saveAndFlush(modality);
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateModalityException();
    }
  }

  @Transactional
  public Modality setActive(Short id, boolean active) {
    Modality modality = find(id);
    modality.setActive(active, OffsetDateTime.now(clock));
    return modality;
  }

  private Modality find(Short id) {
    return modalities.findById(id).orElseThrow(ModalityNotFoundException::new);
  }

  private void ensureAvailable(String name, String slug, Short ignoredId) {
    boolean duplicate =
        ignoredId == null
            ? modalities.existsByNameIgnoreCase(name) || modalities.existsBySlug(slug)
            : modalities.existsByNameIgnoreCaseAndIdNot(name, ignoredId)
                || modalities.existsBySlugAndIdNot(slug, ignoredId);
    if (duplicate) throw new DuplicateModalityException();
  }

  private String normalizeName(String name) {
    return name.trim().replaceAll("\\s+", " ");
  }
}
