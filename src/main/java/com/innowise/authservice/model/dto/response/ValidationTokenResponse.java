package com.innowise.authservice.model.dto.response;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationTokenResponse {

  private Long userId;
  private boolean valid;
  private String email;
  private String role;
  private Date expiresAt;
}
