package com.innowise.authservice.service;

import com.innowise.authservice.model.dto.request.UserRegistrationDto;
import com.innowise.authservice.model.dto.response.UserResponseDto;

public interface UserServiceClient {

  UserResponseDto create(UserRegistrationDto dto);

  void delete(Long id);

}
