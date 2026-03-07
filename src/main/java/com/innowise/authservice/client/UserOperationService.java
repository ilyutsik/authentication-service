package com.innowise.authservice.client;

import com.innowise.authservice.exception.UserRegistrationException;
import com.innowise.authservice.model.dto.request.UserRegistrationDto;
import com.innowise.authservice.model.dto.response.UserResponseDto;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserOperationService {

  private final UserClient userClient;

  @CircuitBreaker(name = "userService", fallbackMethod = "userFallbackCreate")
  public UserResponseDto create(UserRegistrationDto dto) {
    return userClient.create(dto);
  }

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

    throw new UserRegistrationException(HttpStatus.INTERNAL_SERVER_ERROR,
        t.getMessage() + dto.getName(), t);
  }

  private UserResponseDto userFallbackDelete(Long id, Throwable t) {
    if (t instanceof FeignException ex) {

      HttpStatus status = HttpStatus.valueOf(ex.status());
      String body = ex.contentUTF8();

      throw new UserRegistrationException(status, body, ex);
    }

    throw new UserRegistrationException(HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage() + id, t);
  }
}