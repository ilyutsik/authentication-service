package com.innowise.authservice.repository;

import com.innowise.authservice.model.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByUserId(Long id);

  void deleteByUserId(Long id);

  void deleteByToken(String token);

  @Query(nativeQuery = true, value = "delete from refresh_tokens where expiration_date <?1")
  void deleteExpirationTokens(LocalDateTime now);
}
