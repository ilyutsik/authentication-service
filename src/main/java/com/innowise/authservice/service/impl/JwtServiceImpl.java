package com.innowise.authservice.service.impl;

import com.innowise.authservice.config.security.AuthUserDetails;
import com.innowise.authservice.model.dto.response.AuthenticationResponse;
import com.innowise.authservice.model.entity.type.RoleType;
import com.innowise.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtServiceImpl implements JwtService {

  @Value("${jwt.secret}")
  String secret;

  @Value("${jwt.expiration}")
  Long tokenExpiration;

  @Value("${jwt.refresh-expiration}")
  Long refreshTokenExpiration;

  @Override
  public AuthenticationResponse generateAuthToken(UserDetails userDetails) {
    AuthenticationResponse authResponse = new AuthenticationResponse();
    authResponse.setToken(generateToken(userDetails, tokenExpiration));
    authResponse.setRefreshToken(generateToken(userDetails, refreshTokenExpiration));
    return authResponse;
  }

  @Override
  public AuthenticationResponse refreshToken(String refreshToken, UserDetails userDetails) {
    AuthenticationResponse authResponse = new AuthenticationResponse();
    authResponse.setToken(generateToken(userDetails, tokenExpiration));
    authResponse.setRefreshToken(refreshToken);
    return authResponse;
  }

  @Override
  public boolean isInvalid(String token) {
    try {
      extractAllClaims(token);
      return false;
    } catch (ExpiredJwtException e) {
      log.warn("JWT token is expired: {}", e.getMessage());
    } catch (MalformedJwtException | UnsupportedJwtException | SecurityException e) {
      log.warn("Invalid JWT token: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected JWT validation error", e);
    }
    return true;
  }

  @Override
  public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  @Override
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  @Override
  public Long extractUserId(String token) {
    return extractClaim(token, claims -> claims.get("userId", Long.class));
  }

  @Override
  public RoleType extractRole(String token) {
    return extractClaim(token, claims -> RoleType.valueOf(claims.get("role", String.class)));
  }

  private String generateToken(UserDetails userDetails, Long expiration) {
    Map<String, Object> claims = new HashMap<>();
    String subject  = null;
    if (userDetails instanceof AuthUserDetails authUserDetails) {
      claims.put("userId", authUserDetails.getUserId());
      claims.put("role", authUserDetails.getRole().name());
      subject = authUserDetails.getEmail();
    }

    long now = System.currentTimeMillis();
    Date expire = new Date(now + expiration);

    return Jwts.builder()
        .setSubject(subject)
        .addClaims(claims)
        .setIssuedAt(new Date(now))
        .setExpiration(expire).
        signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private <T> T extractClaim(String token, Function<Claims, T> function) {
    Claims claims = extractAllClaims(token);
    return function.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
        .getBody();
  }

  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes());
  }
}
