package com.fitterapp.auth.security;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import com.fitterapp.user.entity.RoleName;

@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration duration;

    public JwtAccessTokenIssuer(
            JwtEncoder jwtEncoder,
            @Value("${fitterapp.jwt.issuer}") String issuer,
            @Value("${fitterapp.jwt.access-token-duration}") Duration duration) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.duration = duration;
    }

    @Override
    public IssuedAccessToken issue(
            UUID userId,
            String email,
            Set<RoleName> roles,
            OffsetDateTime issuedAt) {
        OffsetDateTime expiresAt = issuedAt.plus(duration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .issuedAt(issuedAt.toInstant())
                .expiresAt(expiresAt.toInstant())
                .claim("email", email)
                .claim("roles", roles.stream().map(Enum::name).sorted().toList())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedAccessToken(value, expiresAt);
    }
}
