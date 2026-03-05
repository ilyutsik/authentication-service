package com.innowise.authservice.exception;

public class AuthUserAlreadyExistsException extends RuntimeException {

  public AuthUserAlreadyExistsException(String field, String value) {
    super("User with " + field + ": " + value + " already exist");
  }
}
