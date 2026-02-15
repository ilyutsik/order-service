package com.innowise.orderservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemCreateDto {

  @NotNull(message = "Item ID must be provided")
  private Long itemId;

  @NotNull(message = "Quantity must be provided")
  @Positive(message = "Quantity must be greater than zero")
  private Integer quantity;
}