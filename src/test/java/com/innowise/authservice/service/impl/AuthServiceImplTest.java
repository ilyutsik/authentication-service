package com.innowise.authservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.authservice.config.jwt.JwtService;
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
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.model.entity.type.RoleType;
import com.innowise.authservice.repository.UserRepository;
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
  UserRepository userRepository;
  @Mock
  PasswordEncoder passwordEncoder;

  @Mock
  AuthenticationManager authenticationManager;

  @Mock
  JwtService jwtService;

  @Mock
  CustomUserDetailsService customUserDetailsService;

  @Spy
  UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  private UserDetails userDetails;
  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setEmail("test@mail.com");
    user.setUsername("andrei");
    user.setPassword("12345678");

    this.userDetails = user;
  }

  @Test
  void register_Success() {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("andrei");
    userRequest.setEmail("test@mail.com");
    userRequest.setPassword("12345678");

    when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());
    when(userRepository.findByUsername("andrei")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("12345678")).thenReturn("87654321");

    authService.register(userRequest);

    verify(userRepository, times(1)).findByEmail("test@mail.com");
    verify(userRepository, times(1)).findByUsername("andrei");
    verify(passwordEncoder, times(1)).encode("12345678");
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void register_UserAlreadyExist_withEmail() {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("andrei");
    userRequest.setEmail("test@mail.com");
    userRequest.setPassword("12345678");

    when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(new User()));

    assertThrows(UserAlreadyExistsException.class, () -> authService.register(userRequest));

    verify(userRepository, times(1)).findByEmail("test@mail.com");
  }

  @Test
  void register_UserAlreadyExist_withUsername() {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("andrei");
    userRequest.setEmail("test@mail.com");
    userRequest.setPassword("12345678");

    when(userRepository.findByUsername("andrei")).thenReturn(Optional.of(new User()));

    assertThrows(UserAlreadyExistsException.class, () -> authService.register(userRequest));

    verify(userRepository, times(1)).findByUsername("andrei");
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

    when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(jwtService.generateAuthToken(userDetails)).thenReturn(authenticationResponse);

    AuthenticationResponse response = authService.login(loginRequest);

    assertEquals("token", response.getToken());
    assertEquals("refreshToken", response.getRefreshToken());

    verify(userRepository, times(1)).findByEmail("test@mail.com");
    verify(authenticationManager, times(1)).authenticate(any());
    verify(authentication, times(1)).getPrincipal();
    verify(jwtService, times(1)).generateAuthToken(userDetails);
  }

  @Test
  void login_UserNotFound() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test@mail.com");
    loginRequest.setPassword("12345678");

    when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> authService.login(loginRequest));

    verify(userRepository, times(1)).findByEmail("test@mail.com");
  }

  @Test
  void login_AuthenticationFailed() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test@mail.com");
    loginRequest.setPassword("12345678");


    when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Authentication Failed"));

    assertThrows(AuthenticationFailedException.class, () -> authService.login(loginRequest));

    verify(userRepository, times(1)).findByEmail("test@mail.com");
    verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void refresh_Success() {
    RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
    refreshRequest.setRefreshToken("refreshToken");

    AuthenticationResponse authenticationResponse = new AuthenticationResponse();
    authenticationResponse.setToken("token");
    authenticationResponse.setRefreshToken("refreshToken");

    when(jwtService.isInvalid("refreshToken")).thenReturn(false);
    when(jwtService.extractUsername("refreshToken")).thenReturn("test@mail.com");
    when(customUserDetailsService.loadUserByUsername("test@mail.com")).thenReturn(userDetails);
    when(jwtService.refreshToken("refreshToken", userDetails)).thenReturn(authenticationResponse);

    AuthenticationResponse response = authService.refreshToken(refreshRequest);

    assertEquals("token", response.getToken());
    assertEquals("refreshToken", response.getRefreshToken());

    verify(jwtService, times(1)).isInvalid("refreshToken");
    verify(jwtService, times(1)).extractUsername("refreshToken");
    verify(customUserDetailsService, times(1)).loadUserByUsername("test@mail.com");
    verify(jwtService, times(1)).refreshToken("refreshToken", userDetails);
  }

  @Test
  void refresh_InvalidRefreshToken() {
    RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
    refreshRequest.setRefreshToken("refreshToken");

    when(jwtService.isInvalid("refreshToken")).thenReturn(true);

    assertThrows(InvalidRefreshTokenException.class, () -> authService.refreshToken(refreshRequest));

    verify(jwtService, times(1)).isInvalid("refreshToken");
  }

  @Test
  void refresh_UserNotFound() {
    RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
    refreshRequest.setRefreshToken("refreshToken");

    when(jwtService.isInvalid("refreshToken")).thenReturn(false);
    when(jwtService.extractUsername("refreshToken")).thenReturn("test@mail.com");
    when(customUserDetailsService.loadUserByUsername("test@mail.com")).thenThrow(UserNotFoundException.class);

    assertThrows(UserNotFoundException.class, () -> authService.refreshToken(refreshRequest));

    verify(jwtService, times(1)).isInvalid("refreshToken");
    verify(jwtService, times(1)).extractUsername("refreshToken");
    verify(customUserDetailsService, times(1)).loadUserByUsername("test@mail.com");
  }

  @Test
  void validate_Success() {
    ValidationTokenRequest validationRequest = new ValidationTokenRequest();
    validationRequest.setToken("token");

    Date expiresAt = new Date(System.currentTimeMillis() + 1000);

    when(jwtService.isInvalid("token")).thenReturn(false);
    when(jwtService.extractUsername("token")).thenReturn("test@mail.com");
    when(customUserDetailsService.loadUserByUsername("test@mail.com")).thenReturn(userDetails);
    when(jwtService.extractRole("token")).thenReturn(RoleType.USER);
    when(jwtService.extractUserId("token")).thenReturn(1L);
    when(jwtService.extractExpiration("token")).thenReturn(expiresAt);

    ValidationTokenResponse response = authService.validateToken(validationRequest);

    assertEquals("test@mail.com", response.getEmail());
    assertEquals(expiresAt, response.getExpiresAt());

    verify(jwtService, times(1)).isInvalid("token");
    verify(jwtService, times(1)).extractUsername("token");
    verify(customUserDetailsService, times(1)).loadUserByUsername("test@mail.com");
    verify(jwtService, times(1)).extractRole("token");
    verify(jwtService, times(1)).extractUserId("token");
    verify(jwtService, times(1)).extractExpiration("token");
  }

  @Test
  void validate_InvalidToken() {
    ValidationTokenRequest validationRequest = new ValidationTokenRequest();
    validationRequest.setToken("token");

    when(jwtService.isInvalid("token")).thenReturn(true);

    assertThrows(InvalidTokenException.class, () -> authService.validateToken(validationRequest));

    verify(jwtService, times(1)).isInvalid("token");
  }

  @Test
  void validate_TokenValidationException() {
    ValidationTokenRequest validationRequest = new ValidationTokenRequest();
    validationRequest.setToken("token");

    when(jwtService.isInvalid("token")).thenReturn(false);
    when(jwtService.extractUsername("token")).thenReturn("test@mail.com");
    when(customUserDetailsService.loadUserByUsername("test@mail.com")).thenThrow(UserNotFoundException.class);

    assertThrows(TokenValidationException.class, () -> authService.validateToken(validationRequest));

    verify(jwtService, times(1)).isInvalid("token");
    verify(jwtService, times(1)).extractUsername("token");
    verify(customUserDetailsService, times(1)).loadUserByUsername("test@mail.com");
  }

}
