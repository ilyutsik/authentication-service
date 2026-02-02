package com.innowise.authservice.config.jwt;

import com.innowise.authservice.model.dto.response.AuthenticationResponse;
import com.innowise.authservice.model.entity.User;
import com.innowise.authservice.model.entity.type.RoleType;
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
public class JwtService {

  @Value("${jwt.secret}")
  String secret;

  @Value("${jwt.expiration}")
  Long tokenExpiration;

  @Value("${jwt.refresh-expiration}")
  Long refreshTokenExpiration;

  public AuthenticationResponse generateAuthToken(UserDetails userDetails) {
    AuthenticationResponse authResponse = new AuthenticationResponse();
    authResponse.setToken(generateToken(userDetails));
    authResponse.setRefreshToken(generateRefreshToken(userDetails));
    return authResponse;
  }

  public AuthenticationResponse refreshToken(String refreshToken, UserDetails userDetails) {
    AuthenticationResponse authResponse = new AuthenticationResponse();
    authResponse.setToken(generateToken(userDetails));
    authResponse.setRefreshToken(refreshToken);
    return authResponse;
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public Long extractUserId(String token) {
    return extractClaim(token, claims -> claims.get("userId", Long.class));
  }

  public RoleType extractRole(String token) {
    return extractClaim(token, claims -> RoleType.valueOf(claims.get("role", String.class)));
  }

  public boolean validateToken(String token, UserDetails userDetails) {
    String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && extractExpiration(token).before(
        new Date()));
  }

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

  private String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();

    if (userDetails instanceof User user) {
      claims.put("userId", user.getId());
      claims.put("role", user.getRole().name());
    }

    String subject = userDetails.getUsername();
    long now = System.currentTimeMillis();
    Date expire = new Date(now + tokenExpiration);

    return Jwts.builder()
        .setSubject(subject)
        .addClaims(claims)
        .setIssuedAt(new Date(now))
        .setExpiration(expire).
        signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private String generateRefreshToken(UserDetails userDetails) {
    long now = System.currentTimeMillis();
    Date expire = new Date(now + refreshTokenExpiration);
    String subject = userDetails.getUsername();
    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(new Date(now))
        .setExpiration(expire)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
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
