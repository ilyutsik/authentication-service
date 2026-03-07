package com.innowise.authservice.service.impl;

import com.innowise.authservice.client.UserOperationService;
import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.exception.AuthUserAlreadyExistsException;
import com.innowise.authservice.exception.AuthenticationFailedException;
import com.innowise.authservice.exception.InvalidRefreshTokenException;
import com.innowise.authservice.exception.InvalidTokenException;
import com.innowise.authservice.exception.TokenValidationException;
import com.innowise.authservice.exception.UserNotFoundException;
import com.innowise.authservice.mapper.UserMapper;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.RegistrationRequestDto;
import com.innowise.authservice.model.dto.request.UserRegistrationDto;
import com.innowise.authservice.model.dto.request.ValidationTokenRequest;
import com.innowise.authservice.model.dto.response.AuthenticationResponse;
import com.innowise.authservice.model.dto.response.UserResponseDto;
import com.innowise.authservice.model.dto.response.ValidationTokenResponse;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.CustomUserDetailsService;
import com.innowise.authservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AuthUserRepository authUserRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final CustomUserDetailsService customUserDetailsService;
  private final UserOperationService userOperationService;

  @Override
  public void register(RegistrationRequestDto registrationRequestDto) {
    if (authUserRepository.findByEmail(registrationRequestDto.getEmail()).isPresent()) {
      log.error("User already exist with email");
      throw new AuthUserAlreadyExistsException("email", registrationRequestDto.getEmail());
    }
    if (authUserRepository.findByUsername(registrationRequestDto.getUsername()).isPresent()) {
      log.error("User already exist with username: {}", registrationRequestDto.getUsername());
      throw new AuthUserAlreadyExistsException("username", registrationRequestDto.getUsername());
    }
    UserRegistrationDto userRegistrationDto = userMapper.toUserRegistrationDto(
        registrationRequestDto);

    UserResponseDto createdUser = userOperationService.create(userRegistrationDto);

    try {
      AuthUser newAuthUser = userMapper.toEntity(registrationRequestDto);
      newAuthUser.setId(createdUser.getId());
      newAuthUser.setPassword(passwordEncoder.encode(registrationRequestDto.getPassword()));

      authUserRepository.save(newAuthUser);
    } catch (Exception ex) {

      if (createdUser != null && createdUser.getId() != null) {
        try {
          userOperationService.delete(createdUser.getId());
        } catch (Exception rollbackEx) {
          log.error("Rollback failed for user id {}", createdUser.getId(), rollbackEx);
        }
      }
      throw ex;
    }
  }

  @Override
  public AuthenticationResponse login(LoginRequest loginRequest) {
    if (authUserRepository.findByEmail(loginRequest.getEmail()).isEmpty()) {
      log.error("User not found with email");
      throw new UserNotFoundException("email", loginRequest.getEmail());
    }

    UserDetails userDetails;
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
              loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      userDetails = (UserDetails) authentication.getPrincipal();
    } catch (AuthenticationException ex) {
      log.error("Incorrect email or password");
      throw new AuthenticationFailedException("Incorrect email or password");
    }
    log.info("Generate new jwt token for user");
    return jwtService.generateAuthToken(userDetails);
  }

  @Override
  public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
    String refreshToken = refreshTokenRequest.getRefreshToken();
    if (jwtService.isInvalid(refreshToken)) {
      log.error("Invalid refresh token");
      throw new InvalidRefreshTokenException();
    }
    String email = jwtService.extractEmail(refreshToken);
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
    return jwtService.refreshToken(refreshToken, userDetails);
  }

  @Override
  public ValidationTokenResponse validateToken(ValidationTokenRequest validationTokenRequest) {
    String token = validationTokenRequest.getToken();

    if (jwtService.isInvalid(token)) {
      log.error("Invalid token");
      throw new InvalidTokenException();
    }

    try {
      String username = jwtService.extractEmail(token);
      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

      if (userDetails instanceof AuthUserDetails authUserDetails
          && (!authUserDetails.isAccountNonLocked() || !authUserDetails.isEnabled())) {
        log.warn("Token belongs to disabled or locked user");
        throw new InvalidTokenException();
      }

      return ValidationTokenResponse.builder()
          .valid(true)
          .email(username)
          .role(String.valueOf(jwtService.extractRole(token)))
          .userId(jwtService.extractUserId(token))
          .expiresAt(jwtService.extractExpiration(token))
          .build();
    } catch (Exception e) {
      log.error("Unexpected token validation error: {}", e.getMessage());
      throw new TokenValidationException("Failed validate token: " + e.getMessage());
    }
  }
}
