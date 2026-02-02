package com.innowise.authservice.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ValidationTokenRequest {

  @Size(min = 50, max = 2000, message = "Token length is invalid")
  String token;
}
