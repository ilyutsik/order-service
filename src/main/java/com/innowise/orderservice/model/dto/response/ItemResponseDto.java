package com.innowise.orderservice.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class ItemResponseDto {

  private Long id;
  private String name;
  private BigDecimal price;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

