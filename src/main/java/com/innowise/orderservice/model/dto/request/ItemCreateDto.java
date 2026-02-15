package com.innowise.orderservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateDto {

  @NotBlank(message = "Item name must not be blank")
  private String name;

  @NotNull(message = "Item price must be provided")
  @Positive(message = "Item price must be a positive number")
  private BigDecimal price;
}