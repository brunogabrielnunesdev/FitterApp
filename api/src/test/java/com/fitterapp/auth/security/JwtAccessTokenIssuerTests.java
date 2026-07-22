package com.fitterapp.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.fitterapp.user.entity.RoleName;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

class JwtAccessTokenIssuerTests {

    @Test
    void issuesSignedTokenWithIdentityRolesAndExpiration() {
        SecretKey key = new SecretKeySpec(
                "test-secret-with-at-least-thirty-two-bytes".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
        JwtAccessTokenIssuer issuer = new JwtAccessTokenIssuer(
                new NimbusJwtEncoder(new ImmutableSecret<>(key)),
                "fitterapp-api",
                Duration.ofMinutes(15));
        UUID userId = UUID.randomUUID();
        OffsetDateTime issuedAt = OffsetDateTime.ofInstant(
                Instant.now().truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC);

        IssuedAccessToken issued = issuer.issue(
                userId,
                "bruno@fitterapp.com",
                Set.of(RoleName.PERSONAL, RoleName.STUDENT),
                issuedAt);

        Jwt jwt = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build()
                .decode(issued.value());
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("fitterapp-api");
        assertThat(jwt.getSubject()).isEqualTo(userId.toString());
        assertThat(jwt.getIssuedAt()).isEqualTo(issuedAt.toInstant());
        assertThat(jwt.getExpiresAt()).isEqualTo(issuedAt.plusMinutes(15).toInstant());
        assertThat(jwt.getClaimAsString("email")).isEqualTo("bruno@fitterapp.com");
        assertThat(jwt.getClaimAsStringList("roles"))
                .isEqualTo(List.of("PERSONAL", "STUDENT"));
        assertThat(issued.expiresAt()).isEqualTo(issuedAt.plusMinutes(15));
    }
}
