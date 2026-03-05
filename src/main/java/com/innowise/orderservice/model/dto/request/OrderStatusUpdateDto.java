package com.innowise.orderservice.model.dto.request;

import com.innowise.orderservice.model.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
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
public class OrderStatusUpdateDto {

  @NotNull(message = "Order status is required")
  private OrderStatus status;
}
