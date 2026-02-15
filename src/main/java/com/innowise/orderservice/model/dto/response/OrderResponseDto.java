package com.innowise.orderservice.model.dto.response;

import com.innowise.orderservice.model.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

  private Long id;
  private Long userId;
  private OrderStatus status;
  private BigDecimal totalPrice;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<OrderItemResponseDto> items;
  private UserResponseDto user;
}
