package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;

public interface OrderService {

  OrderResponseDto create(OrderCreateDto dto, Long userId);

  OrderResponseDto getById(Long id);

  Page<OrderResponseDto> get(int page, int size, LocalDateTime from, LocalDateTime to,
      List<OrderStatus> statuses);

  List<OrderResponseDto> getByUserId(Long id);

  OrderResponseDto updateById(Long id, OrderUpdateDto updateDto);

  void deleteById(Long id);
}
