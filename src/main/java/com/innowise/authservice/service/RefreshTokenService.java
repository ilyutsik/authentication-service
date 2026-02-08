package com.innowise.authservice.service;

public interface RefreshTokenService {

  void save(String token);

  void delete(String oldRefreshToken);

  void deleteExpiredTokens();
}
