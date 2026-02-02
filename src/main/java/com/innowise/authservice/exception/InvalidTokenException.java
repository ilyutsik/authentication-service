package com.innowise.authservice.exception;

public class InvalidTokenException extends RuntimeException {

  public InvalidTokenException() {
    super("Invalid token");
  }
}
