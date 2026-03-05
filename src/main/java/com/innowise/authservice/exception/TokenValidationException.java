package com.innowise.authservice.exception;

public class TokenValidationException extends RuntimeException {

  public TokenValidationException(String massage) {
    super(massage);
  }
}
