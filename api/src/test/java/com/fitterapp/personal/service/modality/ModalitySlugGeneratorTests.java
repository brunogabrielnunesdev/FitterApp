package com.fitterapp.personal.service.modality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fitterapp.personal.exception.InvalidModalityNameException;
import org.junit.jupiter.api.Test;

class ModalitySlugGeneratorTests {
  private final ModalitySlugGenerator generator = new ModalitySlugGenerator();

  @Test
  void transliteratesPortugueseCharacters() {
    assertThat(generator.generate("Preparação Física")).isEqualTo("preparacao-fisica");
  }

  @Test
  void rejectsNameWithoutLettersOrNumbers() {
    assertThatThrownBy(() -> generator.generate("---"))
        .isInstanceOf(InvalidModalityNameException.class);
  }
}
