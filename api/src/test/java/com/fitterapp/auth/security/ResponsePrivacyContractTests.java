package com.fitterapp.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fitterapp.personal.dto.publicprofile.PublicProfileCardDto;
import com.fitterapp.personal.dto.publicprofile.PublicProfileDetailDto;
import com.fitterapp.personal.dto.publicprofile.PublicProfilePageDto;
import com.fitterapp.user.dto.admin.AdminUserDetailDto;
import com.fitterapp.user.dto.admin.AdminUserSummaryDto;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ResponsePrivacyContractTests {
  @Test
  void publicProfileContractsDoNotExposePrivateContactOrCrefData() {
    assertNoComponentsContaining(
        List.of(PublicProfileCardDto.class, PublicProfileDetailDto.class, PublicProfilePageDto.class),
        "email",
        "phone",
        "whatsapp",
        "cref",
        "document",
        "password",
        "token");
  }

  @Test
  void administrativeUserContractsDoNotExposeCredentialsOrTokens() {
    assertNoComponentsContaining(
        List.of(AdminUserSummaryDto.class, AdminUserDetailDto.class),
        "password",
        "hash",
        "token",
        "secret");
  }

  private void assertNoComponentsContaining(List<Class<?>> contracts, String... forbiddenTerms) {
    for (Class<?> contract : contracts) {
      assertThat(contract.isRecord()).isTrue();
      List<String> componentNames =
          Arrays.stream(contract.getRecordComponents())
              .map(RecordComponent::getName)
              .map(name -> name.toLowerCase(Locale.ROOT))
              .toList();

      for (String term : forbiddenTerms) {
        assertThat(componentNames)
            .as("%s must not expose %s", contract.getSimpleName(), term)
            .noneMatch(name -> name.contains(term));
      }
    }
  }
}
