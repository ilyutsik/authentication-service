package com.innowise.authservice.exception;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

  private int status;
  private LocalDateTime localDateTime;
  private String error;
  private Map<String, String> errors;
}
