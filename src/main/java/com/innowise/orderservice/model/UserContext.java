package com.innowise.orderservice.model;

import com.innowise.orderservice.model.dto.response.UserResponseDto;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserContext {

  private Long id;
  private String name;
  private String surname;
  private LocalDate birthDate;
  private String email;
  private Boolean active;

  public static UserContext from(UserResponseDto dto) {
    if (dto == null) {
      return null;
    } else return new UserContext(
        dto.getId(),
        dto.getName(),
        dto.getSurname(),
        dto.getBirthDate(),
        dto.getEmail(),
        dto.getActive());
  }
}