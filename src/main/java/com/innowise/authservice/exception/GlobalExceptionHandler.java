package com.innowise.authservice.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
    log.error("User not found: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(),
        LocalDateTime.now(), "User Not Found", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExist(UserAlreadyExistsException ex) {
    log.error("User already exist: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.CONFLICT.value(),
        LocalDateTime.now(), "User Already Exist", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(TokenValidationException.class)
  public ResponseEntity<ErrorResponse> handleTokenValidation(TokenValidationException ex) {
    log.error("Token validation exception: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
        LocalDateTime.now(), "Token Validation Exception", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
    log.error("Invalid token exception: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
        LocalDateTime.now(), "Invalid Token Exception", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
    log.error("Invalid refresh token exception: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
        LocalDateTime.now(), "Invalid Refresh Token Exception", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AuthenticationFailedException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationFailed(
      AuthenticationFailedException ex) {
    log.error("Authentication failed: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
        LocalDateTime.now(), "Authentication Failed", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    log.error("Access denied: {}", ex.getMessage());
    ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), LocalDateTime.now(),
        "Access Denied", "Insufficient Permissions");
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
    log.error("Bad credentials: {}", ex.getMessage());
    ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now(),
        "Authentication Failed", "Invalid Email Or Password");
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex) {
    log.error("JWT expired: {}", ex.getMessage());
    ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now(),
        "Token Expired", "JWT Token Has Expired");
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(SignatureException.class)
  public ResponseEntity<ErrorResponse> handleSignatureException(SignatureException ex) {
    log.error("JWT signature invalid: {}", ex.getMessage());
    ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now(),
        "Invalid Token", "JWT Signature Is Invalid");
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    log.error("Validation failed: {}", ex.getMessage());
    Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(
        Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
            (msg1, msg2) -> msg1 + "; " + msg2));

    ValidationErrorResponse response = new ValidationErrorResponse(HttpStatus.BAD_REQUEST.value(),
        LocalDateTime.now(), "Validation Failed", errors);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        LocalDateTime.now(), "Internal Server Error", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
