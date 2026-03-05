package com.innowise.authservice.exception;

public class AuthenticationFailedException extends RuntimeException {

  public AuthenticationFailedException(String massage) {
    super(massage);
  }
}
