package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.response.AuthenticationResponse;
import com.innowise.authservice.model.entity.type.RoleType;
import java.util.Date;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

  AuthenticationResponse generateAuthToken(UserDetails userDetails);

  AuthenticationResponse refreshToken(String refreshToken, UserDetails userDetails);

  boolean isInvalid(String token);

  String extractEmail(String token);

  Date extractExpiration(String token);

  Long extractUserId(String token);

  RoleType extractRole(String token);
}
