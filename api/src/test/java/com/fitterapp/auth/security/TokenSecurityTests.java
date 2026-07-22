package com.fitterapp.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenSecurityTests {

    @Test
    void hashesTokenWithSha256AsLowercaseHexadecimal() {
        TokenHasher tokenHasher = new TokenHasher();

        assertThat(tokenHasher.hash("fitterapp"))
                .isEqualTo("a9c1354ad3b03d48815666f01ff9279db3b65e6c1d3f67458c8e6d60f01e3b91");
    }

    @Test
    void generatesDistinctUrlSafeTokensWith256Bits() {
        SecureTokenGenerator tokenGenerator = new SecureTokenGenerator();

        String first = tokenGenerator.generate();
        String second = tokenGenerator.generate();

        assertThat(first)
                .hasSize(43)
                .matches("^[A-Za-z0-9_-]+$");
        assertThat(second).isNotEqualTo(first);
    }
}
