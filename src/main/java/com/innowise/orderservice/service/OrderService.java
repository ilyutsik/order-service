package com.innowise.orderservice.service;

import com.innowise.orderservice.model.UserContext;
import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.request.OrderItemUpdateDto;
import com.innowise.orderservice.model.dto.request.OrderStatusUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.OrderWithUserResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.model.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;

public interface OrderService {

  OrderWithUserResponseDto create(OrderCreateDto orderDto,  Long userId);

  OrderWithUserResponseDto getById(Long id);

  Page<OrderWithUserResponseDto> get(int page, int size, LocalDateTime from, LocalDateTime to,
      List<OrderStatus> statuses);

  List<OrderWithUserResponseDto> getByUserId(Long userId);

  OrderWithUserResponseDto updateStatusById(Long id, OrderStatusUpdateDto updateDto);

  OrderWithUserResponseDto updateItemById(Long id, OrderItemUpdateDto updateDto);

  void deleteById(Long id);
}
