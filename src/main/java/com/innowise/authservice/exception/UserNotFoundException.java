package com.innowise.authservice.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String field, String value) {
    super("User with " + field + ":" + value + " not found");
  }
}
