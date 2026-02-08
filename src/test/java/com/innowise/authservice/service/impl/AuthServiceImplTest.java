package com.innowise.authservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.exception.AuthenticationFailedException;
import com.innowise.authservice.exception.InvalidRefreshTokenException;
import com.innowise.authservice.exception.InvalidTokenException;
import com.innowise.authservice.exception.TokenValidationException;
import com.innowise.authservice.exception.UserAlreadyExistsException;
import com.innowise.authservice.exception.UserNotFoundException;
import com.innowise.authservice.mapper.UserMapper;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.UserRequest;
import com.innowise.authservice.model.dto.request.ValidationTokenRequest;
import com.innowise.authservice.model.dto.response.AuthenticationResponse;
import com.innowise.authservice.model.dto.response.ValidationTokenResponse;
import com.innowise.authservice.model.entity.AuthUser;
import com.innowise.authservice.model.entity.type.RoleType;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.service.CustomUserDetailsService;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @InjectMocks
  AuthServiceImpl authService;

  @Mock
  AuthUserRepository authUserRepository;
  @Mock
  PasswordEncoder passwordEncoder;

  @Mock
  AuthenticationManager authenticationManager;

  @Mock
  JwtServiceImpl jwtServiceImpl;

  @Mock
  CustomUserDetailsService customUserDetailsService;

  @Spy
  UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  private UserDetails userDetails;
  private AuthUser authUser;

  @BeforeEach
  void setUp() {
    authUser = new AuthUser();
    authUser.setId(1L);
    authUser.setEmail("test@mail.com");
    authUser.setUsername("andrei");
    authUser.setPassword("12345678");
    authUser.setAccountNotLocked(true);

    this.userDetails = new AuthUserDetails(authUser);
  }

  @Test
  void register_Success() {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("andrei");
    userRequest.setEmail("test@mail.com");
    userRequest.setPassword("12345678");

    when(authUserRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());
    when(authUserRepository.findByUsername("andrei")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("12345678")).thenReturn("87654321");

    authService.register(userRequest);

    verify(authUserRepository, times(1)).findByEmail("test@mail.com");
    verify(authUserRepository, times(1)).findByUsername("andrei");
    verify(passwordEncoder, times(1)).encode("12345678");
    verify(authUserRepository, times(1)).save(any(AuthUser.class));
  }

  @Test
  void register_UserAlreadyExist_withEmail() {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("andrei");
    userRequest.setEmail("test@mail.com");
    userRequest.setPassword("12345678");

    when(authUserRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(new AuthUser()));

    assertThrows(UserAlreadyExistsException.class, () -> authService.register(userRequest));

    verify(authUserRepository, times(1)).findByEmail("test@mail.com");
  }

  @Test
  void register_UserAlreadyExist_withUsername() {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("andrei");
    userRequest.setEmail("test@mail.com");
    userRequest.setPassword("12345678");

    when(authUserRepository.findByUsername("andrei")).thenReturn(Optional.of(new AuthUser()));

    assertThrows(UserAlreadyExistsException.class, () -> authService.register(userRequest));

    verify(authUserRepository, times(1)).findByUsername("andrei");
  }

  @Test
  void login_Success() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test@mail.com");
    loginRequest.setPassword("12345678");

    Authentication authentication = mock(Authentication.class);

    AuthenticationResponse authenticationResponse = new AuthenticationResponse();
    authenticationResponse.setToken("token");
    authenticationResponse.setRefreshToken("refreshToken");

    when(authUserRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(
        authUser));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(jwtServiceImpl.generateAuthToken(userDetails)).thenReturn(authenticationResponse);

    AuthenticationResponse response = authService.login(loginRequest);

    assertEquals("token", response.getToken());
    assertEquals("refreshToken", response.getRefreshToken());

    verify(authUserRepository, times(1)).findByEmail("test@mail.com");
    verify(authenticationManager, times(1)).authenticate(any());
    verify(authentication, times(1)).getPrincipal();
    verify(jwtServiceImpl, times(1)).generateAuthToken(userDetails);
  }

  @Test
  void login_UserNotFound() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test@mail.com");
    loginRequest.setPassword("12345678");

    when(authUserRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest));

    verify(authUserRepository, times(1)).findByEmail("test@mail.com");
  }

  @Test
  void login_AuthenticationFailed() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test@mail.com");
    loginRequest.setPassword("12345678");


    when(authUserRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(
        authUser));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Authentication Failed"));

    assertThrows(AuthenticationFailedException.class, () -> authService.login(loginRequest));

    verify(authUserRepository, times(1)).findByEmail("test@mail.com");
    verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void refresh_Success() {
    RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
    refreshRequest.setRefreshToken("refreshToken");

    AuthenticationResponse authenticationResponse = new AuthenticationResponse();
    authenticationResponse.setToken("token");
    authenticationResponse.setRefreshToken("refreshToken");

    when(jwtServiceImpl.isInvalid("refreshToken")).thenReturn(false);
    when(jwtServiceImpl.extractEmail("refreshToken")).thenReturn("test@mail.com");
    when(customUserDetailsService.loadUserByUsername("test@mail.com")).thenReturn(userDetails);
    when(jwtServiceImpl.refreshToken("refreshToken", userDetails)).thenReturn(authenticationResponse);

    AuthenticationResponse response = authService.refreshToken(refreshRequest);

    assertEquals("token", response.getToken());
    assertEquals("refreshToken", response.getRefreshToken());

    verify(jwtServiceImpl, times(1)).isInvalid("refreshToken");
    verify(jwtServiceImpl, times(1)).extractEmail("refreshToken");
    verify(customUserDetailsService, times(1)).loadUserByUsername("test@mail.com");
    verify(jwtServiceImpl, times(1)).refreshToken("refreshToken", userDetails);
  }

  @Test
  void refresh_InvalidRefreshToken() {
    RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
    refreshRequest.setRefreshToken("refreshToken");

    when(jwtServiceImpl.isInvalid("refreshToken")).thenReturn(true);

    assertThrows(InvalidRefreshTokenException.class, () -> authService.refreshToken(refreshRequest));

    verify(jwtServiceImpl, times(1)).isInvalid("refreshToken");
  }

  @Test
  void refresh_UserNotFound() {
    RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
    refreshRequest.setRefreshToken("refreshToken");

    when(jwtServiceImpl.isInvalid("refreshToken")).thenReturn(false);
    when(jwtServiceImpl.extractEmail("refreshToken")).thenReturn("test@mail.com");
    when(customUserDetailsService.loadUserByUsername("test@mail.com")).thenThrow(UserNotFoundException.class);

    assertThrows(UserNotFoundException.class, () -> authService.refreshToken(refreshRequest));

    verify(jwtServiceImpl, times(1)).isInvalid("refreshToken");
    verify(jwtServiceImpl, times(1)).extractEmail("refreshToken");
    verify(customUserDetailsService, times(1)).loadUserByUsername("test@mail.com");
  }

  @Test
  void validate_Success() {
    ValidationTokenRequest validationRequest = new ValidationTokenRequest();
    validationRequest.setToken("token");

    Date expiresAt = new Date(System.currentTimeMillis() + 1000);

    when(jwtServiceImpl.isInvalid("token")).thenReturn(false);
    when(jwtServiceImpl.extractEmail("token")).thenReturn("test@mail.com");
    when(customUserDetailsService.loadUserByUsername("test@mail.com")).thenReturn(userDetails);
    when(jwtServiceImpl.extractRole("token")).thenReturn(RoleType.USER);
    when(jwtServiceImpl.extractUserId("token")).thenReturn(1L);
    when(jwtServiceImpl.extractExpiration("token")).thenReturn(expiresAt);

    ValidationTokenResponse response = authService.validateToken(validationRequest);

    assertEquals("test@mail.com", response.getEmail());
    assertEquals(expiresAt, response.getExpiresAt());

    verify(jwtServiceImpl, times(1)).isInvalid("token");
    verify(jwtServiceImpl, times(1)).extractEmail("token");
    verify(customUserDetailsService, times(1)).loadUserByUsername("test@mail.com");
    verify(jwtServiceImpl, times(1)).extractRole("token");
    verify(jwtServiceImpl, times(1)).extractUserId("token");
    verify(jwtServiceImpl, times(1)).extractExpiration("token");
  }

  @Test
  void validate_InvalidToken() {
    ValidationTokenRequest validationRequest = new ValidationTokenRequest();
    validationRequest.setToken("token");

    when(jwtServiceImpl.isInvalid("token")).thenReturn(true);

    assertThrows(InvalidTokenException.class, () -> authService.validateToken(validationRequest));

    verify(jwtServiceImpl, times(1)).isInvalid("token");
  }

  @Test
  void validate_TokenValidationException() {
    ValidationTokenRequest validationRequest = new ValidationTokenRequest();
    validationRequest.setToken("token");

    when(jwtServiceImpl.isInvalid("token")).thenReturn(false);
    when(jwtServiceImpl.extractEmail("token")).thenReturn("test@mail.com");
    when(customUserDetailsService.loadUserByUsername("test@mail.com")).thenThrow(UserNotFoundException.class);

    assertThrows(TokenValidationException.class, () -> authService.validateToken(validationRequest));

    verify(jwtServiceImpl, times(1)).isInvalid("token");
    verify(jwtServiceImpl, times(1)).extractEmail("token");
    verify(customUserDetailsService, times(1)).loadUserByUsername("test@mail.com");
  }

}
