package com.innowise.orderservice.model.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

  private Long id;

  private String name;

  private String surname;

  private LocalDate birthDate;

  private String email;

  private Boolean active;
}
