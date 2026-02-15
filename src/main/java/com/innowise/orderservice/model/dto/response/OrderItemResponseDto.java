package com.innowise.orderservice.model.dto.response;

import java.math.BigDecimal;
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
public class OrderItemResponseDto {

  private Long orderId;
  private Long itemId;
  private String itemName;
  private BigDecimal itemPrice;
  private Integer quantity;
}