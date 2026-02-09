package com.innowise.authservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.authservice.IntegrationTestBase;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.UserRequest;
import com.innowise.authservice.model.dto.request.ValidationTokenRequest;
import com.innowise.authservice.model.dto.response.AuthenticationResponse;
import com.innowise.authservice.model.dto.response.ValidationTokenResponse;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class AuthControllerTest extends IntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AuthUserRepository authUserRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @BeforeEach
  void setUp() throws Exception {
    refreshTokenRepository.deleteAll();
    authUserRepository.deleteAll();
    registerTestUser();
  }

  private void registerTestUser() throws Exception {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("test1");
    userRequest.setEmail("test1@mail");
    userRequest.setPassword("12345678");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().string("User register successfully"));
  }

  private AuthenticationResponse loginTestUser() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test1@mail");
    loginRequest.setPassword("12345678");

    MvcResult result = mockMvc.perform(
            post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn();

    return objectMapper.readValue(result.getResponse().getContentAsString(),
        AuthenticationResponse.class);
  }

  @Test
  void register_shouldRegisterUserSuccessfully() throws Exception {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("andrei");
    userRequest.setEmail("andrei@mail");
    userRequest.setPassword("12345678");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().string("User register successfully"));
  }

  @Test
  void register_whenUsernameExist_shouldReturnConflict() throws Exception {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("test1");
    userRequest.setEmail("andre@mail");
    userRequest.setPassword("1234567i8");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  void register_whenEmailExist_shouldReturnConflict() throws Exception {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("andrei");
    userRequest.setEmail("test1@mail");
    userRequest.setPassword("1234567i8");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  void register_whenEmailInvalid_shouldReturnBadRequest() throws Exception {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("andrei");
    userRequest.setEmail("andrei");
    userRequest.setPassword("12345678");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_whenEmptyFields_shouldReturnBadRequest() throws Exception {
    UserRequest userRequest = new UserRequest();
    userRequest.setUsername("");
    userRequest.setEmail("");
    userRequest.setPassword("");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_shouldReturnAuthenticationResponse() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test1@mail");
    loginRequest.setPassword("12345678");

    MvcResult result = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn();

    AuthenticationResponse response = objectMapper.readValue(
        result.getResponse().getContentAsString(), AuthenticationResponse.class);
    Assertions.assertNotNull(response.getToken());
    Assertions.assertNotNull(response.getRefreshToken());
  }

  @Test
  void login_whenFieldsEmpty_shouldThrowBedRequest() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("");
    loginRequest.setPassword("");

    mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_whenEmailNotExist_shouldThrowNotFound() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("t2@mail");
    loginRequest.setPassword("12345678");

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void login_whenPasswordWrong_shouldThrowUnauthorized() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test1@mail");
    loginRequest.setPassword("11111111");

    mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refresh_shouldReturnAuthResponse() throws Exception {
    AuthenticationResponse response = loginTestUser();
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken(response.getRefreshToken());

    MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn();

    AuthenticationResponse authResponse = objectMapper.readValue(
        result.getResponse().getContentAsString(), AuthenticationResponse.class);
    Assertions.assertNotNull(authResponse.getToken());
    Assertions.assertNotNull(authResponse.getRefreshToken());
  }

  @Test
  void refresh_whenTokenInvalid_shouldThrowUnauthorized() throws Exception {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("12345678901234567890123456789012345678901234567890");

    mockMvc.perform(post("/api/v1/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refresh_whenTokenEmpty_shouldThrowBedRequest() throws Exception {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("");

    mockMvc.perform(post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void validate_shouldReturnValidationResponse() throws Exception {
    AuthenticationResponse authResponse = loginTestUser();
    ValidationTokenRequest validationRequest = new ValidationTokenRequest();
    validationRequest.setToken(authResponse.getToken());

    MvcResult result = mockMvc.perform(post("/api/v1/auth/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validationRequest)))
        .andExpect(status().isOk())
        .andReturn();

    ValidationTokenResponse response = objectMapper.readValue(
        result.getResponse().getContentAsString(), ValidationTokenResponse.class);

    Assertions.assertNotNull(response.getEmail());
    Assertions.assertNotNull(response.getRole());
    Assertions.assertNotNull(response.getExpiresAt());
    Assertions.assertNotNull(response.getUserId());
  }

  @Test
  void validate_whenTokenInvalid_shouldThrowUnauthorized() throws Exception {
    ValidationTokenRequest validationRequest = new ValidationTokenRequest();
    validationRequest.setToken("12345678901234567890123456789012345678901234567890");

    mockMvc.perform(post("/api/v1/auth/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validationRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void validate_whenTokenEmpty_shouldThrowBedRequest() throws Exception {
    ValidationTokenRequest validationRequest = new ValidationTokenRequest();
    validationRequest.setToken("");

    mockMvc.perform(post("/api/v1/auth/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validationRequest)))
        .andExpect(status().isBadRequest());
  }
}
