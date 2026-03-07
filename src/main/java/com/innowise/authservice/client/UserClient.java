package com.innowise.authservice.client;

import com.innowise.authservice.model.dto.request.UserRegistrationDto;
import com.innowise.authservice.model.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {

  @PostMapping("/api/v1/users")
  UserResponseDto create(@RequestBody UserRegistrationDto dto);

  @DeleteMapping("/api/v1/users/{id}")
  void delete(@PathVariable(name = "id") Long id);
}
