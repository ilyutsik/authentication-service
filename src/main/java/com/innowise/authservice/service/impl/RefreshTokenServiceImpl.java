package com.innowise.authservice.service.impl;

import com.innowise.authservice.exception.InvalidRefreshTokenException;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.RefreshToken;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.repository.RefreshTokenRepository;
import com.innowise.authservice.service.JwtService;
import com.innowise.authservice.service.RefreshTokenService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final AuthUserRepository authUserRepository;
  private final JwtService jwtService;

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void save(String token) {
    Long userId = jwtService.extractUserId(token);
    AuthUser authUser = authUserRepository.findById(userId)
        .orElseThrow(InvalidRefreshTokenException::new);

    if (refreshTokenRepository.findByUserId(userId).isPresent()) {
      refreshTokenRepository.deleteByUserId(userId);
    }
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(authUser);
    refreshToken.setToken(token);
    LocalDateTime expirationDate = jwtService.extractExpiration(token)
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
    refreshToken.setExpirationDate(expirationDate);
    refreshTokenRepository.save(refreshToken);
  }

  @Override
  public void delete(String oldRefreshToken) {
    RefreshToken existing = refreshTokenRepository
        .findByToken(oldRefreshToken)
        .orElseThrow(InvalidRefreshTokenException::new);

    refreshTokenRepository.delete(existing);
  }

  @Override
  @Scheduled(cron = "0 0 * * * *")
  public void deleteExpiredTokens() {
    refreshTokenRepository.deleteExpirationTokens(LocalDateTime.now());
  }
}

