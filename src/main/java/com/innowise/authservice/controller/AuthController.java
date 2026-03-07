package com.innowise.authservice.controller;

import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.RegistrationRequestDto;
import com.innowise.authservice.model.dto.request.ValidationTokenRequest;
import com.innowise.authservice.model.dto.response.AuthenticationResponse;
import com.innowise.authservice.model.dto.response.ValidationTokenResponse;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final RefreshTokenService refreshTokenService;

  @PostMapping("/register")
  public ResponseEntity<String> register(@Valid @RequestBody RegistrationRequestDto request) {
    authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body("User register successfully");
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthenticationResponse response = authService.login(request);
    refreshTokenService.save(response.getRefreshToken());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthenticationResponse> refresh(
      @Valid @RequestBody RefreshTokenRequest request) {
    AuthenticationResponse response = authService.refreshToken(request);
    refreshTokenService.delete(request.getRefreshToken());
    refreshTokenService.save(response.getRefreshToken());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/validate")
  public ResponseEntity<ValidationTokenResponse> validate(
      @Valid @RequestBody ValidationTokenRequest request) {
    ValidationTokenResponse response = authService.validateToken(request);
    return ResponseEntity.ok(response);
  }
}
