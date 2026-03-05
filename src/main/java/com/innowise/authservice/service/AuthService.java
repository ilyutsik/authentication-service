package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.AuthRegistrationDto;
import com.innowise.authservice.model.dto.request.RegistrationRequestDto;
import com.innowise.authservice.model.dto.request.ValidationTokenRequest;
import com.innowise.authservice.model.dto.response.AuthenticationResponse;
import com.innowise.authservice.model.dto.response.ValidationTokenResponse;

public interface AuthService {

  void register(RegistrationRequestDto registrationRequestDto);

  AuthenticationResponse login(LoginRequest loginRequest);

  AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

  ValidationTokenResponse validateToken(ValidationTokenRequest validationTokenRequest);
}
