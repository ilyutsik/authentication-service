package com.innowise.authservice.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serial;
import org.springframework.http.HttpStatus;

public class UserRegistrationException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -2336696123306510959L;

  private final HttpStatus status;
  private final String responseBody;

  public UserRegistrationException(HttpStatus status, String responseBody, Throwable cause) {
    super(extractMessage(responseBody), cause);
    this.status = status;
    this.responseBody = responseBody;
  }

  private static String extractMessage(String responseBody) {
    if (responseBody == null || responseBody.isBlank()) {
      return "User service error";
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode node = mapper.readTree(responseBody);

      if (node.has("message")) {
        return node.get("message").asText();
      }

      return responseBody;
    } catch (Exception e) {
      return responseBody;
    }
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getResponseBody() {
    return responseBody;
  }
}
