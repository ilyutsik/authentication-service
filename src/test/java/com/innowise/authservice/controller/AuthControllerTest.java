package com.innowise.authservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.authservice.IntegrationTestBase;
import com.innowise.authservice.model.dto.request.LoginRequest;
import com.innowise.authservice.model.dto.request.RefreshTokenRequest;
import com.innowise.authservice.model.dto.request.RegistrationRequestDto;
import com.innowise.authservice.model.dto.request.ValidationTokenRequest;
import com.innowise.authservice.model.dto.response.AuthenticationResponse;
import com.innowise.authservice.model.dto.response.ValidationTokenResponse;
import com.innowise.authservice.repository.AuthUserRepository;
import com.innowise.authservice.repository.RefreshTokenRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
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
    WireMock.configureFor(wiremock.getHost(), wiremock.getMappedPort(8080));
    WireMock.reset();
    registerTestUser();
  }

  private void registerTestUser() throws Exception {
    WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/users"))
        .willReturn(WireMock.aResponse()
            .withStatus(201)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "id": %d,
                  "name": "Test",
                  "surname": "User",
                  "email": "%s",
                  "active": true
                }
                """.formatted(1, "test1@mail.com"))
        ));
    RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
    registrationRequestDto.setName("Test");
    registrationRequestDto.setSurname("User");
    registrationRequestDto.setUsername("test1");
    registrationRequestDto.setBirthDate(LocalDate.of(2000, 1, 1));
    registrationRequestDto.setEmail("test1@mail.com");
    registrationRequestDto.setPassword("12345678");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequestDto)))
        .andExpect(status().isCreated())
        .andExpect(content().string("User register successfully"));
  }

  private AuthenticationResponse loginTestUser() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test1@mail.com");
    loginRequest.setPassword("12345678");

    MvcResult result = mockMvc.perform(
            post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn();

    return objectMapper.readValue(result.getResponse().getContentAsString(),
        AuthenticationResponse.class);
  }

  private void stubUser(Long userId) {
    WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/api/v1/users"))
        .willReturn(WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "id": %d,
                  "name": "Andrei",
                  "surname": "Ilyutsik",
                  "email": "ilyutsik.andrei@gmail.com",
                  "active": true
                }
                """.formatted(userId))
        ));
  }

  @Test
  void register_shouldRegisterUserSuccessfully() throws Exception {
    Long userId = 1L;
    stubUser(userId);

    RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
    registrationRequestDto.setName("andrei");
    registrationRequestDto.setSurname("ilyutsik");
    registrationRequestDto.setUsername("andrei");
    registrationRequestDto.setBirthDate(LocalDate.of(2000, 2, 2));
    registrationRequestDto.setEmail("test@mail.com");
    registrationRequestDto.setPassword("12345678");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registrationRequestDto)))
        .andExpect(status().isCreated())
        .andExpect(content().string("User register successfully"));
  }

  @Test
  void register_whenUsernameExist_shouldReturnConflict() throws Exception {
    RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
    registrationRequestDto.setName("andrei");
    registrationRequestDto.setSurname("ilyutsik");
    registrationRequestDto.setUsername("test1");
    registrationRequestDto.setBirthDate(LocalDate.of(2000, 2, 2));
    registrationRequestDto.setEmail("test@mail.com");
    registrationRequestDto.setPassword("12345678");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registrationRequestDto)))
        .andExpect(status().isConflict());
  }

  @Test
  void register_whenEmailExist_shouldReturnConflict() throws Exception {
    RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
    registrationRequestDto.setName("andrei");
    registrationRequestDto.setSurname("ilyutsik");
    registrationRequestDto.setUsername("test1");
    registrationRequestDto.setBirthDate(LocalDate.of(2000, 2, 2));
    registrationRequestDto.setEmail("test1@mail.com");
    registrationRequestDto.setPassword("12345678");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registrationRequestDto)))
        .andExpect(status().isConflict());
  }

  @Test
  void register_whenEmailInvalid_shouldReturnBadRequest() throws Exception {
    RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
    registrationRequestDto.setName("andrei");
    registrationRequestDto.setSurname("ilyutsik");
    registrationRequestDto.setUsername("test1");
    registrationRequestDto.setBirthDate(LocalDate.of(2000, 2, 2));
    registrationRequestDto.setPassword("12345678");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registrationRequestDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_whenEmptyFields_shouldReturnBadRequest() throws Exception {
    RegistrationRequestDto registrationRequestDto = new RegistrationRequestDto();
    registrationRequestDto.setName("");
    registrationRequestDto.setSurname("");
    registrationRequestDto.setUsername("");
    registrationRequestDto.setBirthDate(LocalDate.of(2000, 2, 2));
    registrationRequestDto.setEmail("");
    registrationRequestDto.setPassword("");

    mockMvc.perform(post("/api/v1/auth/register")
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registrationRequestDto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void login_shouldReturnAuthenticationResponse() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test1@mail.com");
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
    loginRequest.setEmail("test1@mail.com");
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
