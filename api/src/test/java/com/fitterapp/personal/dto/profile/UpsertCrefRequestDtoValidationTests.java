package com.fitterapp.personal.dto.profile;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

class UpsertCrefRequestDtoValidationTests {

  @Test
  void rejectsRegistrationCodeWithoutDocument() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var violations = factory.getValidator().validate(new UpsertCrefRequestDto("123-G/PR", null));

      assertThat(violations)
          .extracting(violation -> violation.getPropertyPath().toString())
          .containsExactly("documentImageKey");
    }
  }

  @Test
  void rejectsDocumentWithoutRegistrationCode() {
    try (var factory = Validation.buildDefaultValidatorFactory()) {
      var violations =
          factory
              .getValidator()
              .validate(new UpsertCrefRequestDto(null, "private/crefs/profile/document.webp"));

      assertThat(violations)
          .extracting(violation -> violation.getPropertyPath().toString())
          .containsExactly("registrationCode");
    }
  }
}
