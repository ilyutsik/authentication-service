package com.innowise.authservice.model.dto.response;

import lombok.Data;

@Data
public class AuthenticationResponse {

  String token;

  String refreshToken;
}
