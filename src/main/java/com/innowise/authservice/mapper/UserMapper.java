package com.innowise.authservice.mapper;

import com.innowise.authservice.model.dto.request.UserRequest;
import com.innowise.authservice.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "authorities", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "accountNotExpired", ignore = true)
  @Mapping(target = "accountNotLocked", ignore = true)
  @Mapping(target = "credentialsNotExpired", ignore = true)
  @Mapping(target = "enabled", ignore = true)
  @Mapping(target = "role", ignore = true)
  User toEntity(UserRequest userRequest);
}
