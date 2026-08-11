package com.fitterapp.personal.service.modality;

import com.fitterapp.personal.exception.InvalidModalityNameException;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ModalitySlugGenerator {

  public String generate(String name) {
    String slug =
        Normalizer.normalize(name, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-+|-+$)", "");
    if (slug.isBlank()) throw new InvalidModalityNameException();
    return slug;
  }
}
