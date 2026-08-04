package com.fitterapp.auth.repository;

import com.fitterapp.auth.entity.RefreshToken;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findAllByFamilyId(UUID familyId);

  List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(UUID userId);
}
