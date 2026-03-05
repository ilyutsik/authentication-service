package com.innowise.authservice.service.impl;

import com.innowise.authservice.client.UserClient;
import com.innowise.authservice.exception.UserRegistrationException;
import com.innowise.authservice.model.dto.request.UserRegistrationDto;
import com.innowise.authservice.model.dto.response.UserResponseDto;
import com.innowise.authservice.service.UserServiceClient;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

  private final UserClient userClient;

  @Override
  @CircuitBreaker(name = "userService", fallbackMethod = "userFallbackCreate")
  public UserResponseDto create(UserRegistrationDto dto) {
    return userClient.create(dto);
  }

  @Override
  @CircuitBreaker(name = "userService", fallbackMethod = "userFallbackDelete")
  public void delete(Long id) {
    userClient.delete(id);
  }

  private UserResponseDto userFallbackCreate(UserRegistrationDto dto, Throwable t) {
    if (t instanceof FeignException ex) {

      HttpStatus status = HttpStatus.valueOf(ex.status());
      String body = ex.contentUTF8();

      throw new UserRegistrationException(status, body, ex);
    }

    throw new UserRegistrationException(HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage(), t);
  }

  private UserResponseDto userFallbackDelete(Long id, Throwable t) {
    if (t instanceof FeignException ex) {

      HttpStatus status = HttpStatus.valueOf(ex.status());
      String body = ex.contentUTF8();

      throw new UserRegistrationException(status, body, ex);
    }

    throw new UserRegistrationException(HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage(), t);
  }
}