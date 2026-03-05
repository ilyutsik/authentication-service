package com.innowise.authservice.model.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

  private Long id;

  private String name;

  private String surname;

  private LocalDate birthDate;

  private String email;

  private Boolean active;

  private LocalDate createdAt;

  private LocalDate updatedAt;
}