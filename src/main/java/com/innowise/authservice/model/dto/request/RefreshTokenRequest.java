package com.innowise.authservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefreshTokenRequest {

  @NotBlank(message = "Refresh token is required")
  @Size(min = 50, max = 2000, message = "Refresh token length is invalid")
  String refreshToken;
}
