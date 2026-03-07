package com.innowise.authservice.mapper;

import com.innowise.authservice.model.dto.request.RegistrationRequestDto;
import com.innowise.authservice.model.dto.request.UserRegistrationDto;
import com.innowise.authservice.model.entity.AuthUser;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

  AuthUser toEntity(RegistrationRequestDto dto);

  UserRegistrationDto toUserRegistrationDto(RegistrationRequestDto dto);
}
