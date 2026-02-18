package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.OrderWithUserResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.service.UserServiceClient;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserOrdersController {

  private final OrderService orderService;
  private final UserServiceClient userServiceClient;

  @GetMapping("/{userId}/orders")
  public ResponseEntity<List<OrderWithUserResponseDto>> getOrdersByUserId(
      @PathVariable("userId") Long userId) {
    return ResponseEntity.status(HttpStatus.OK).body(createOrderWithUserResponse(userId));
  }

  @GetMapping("/orders")
  public ResponseEntity<List<OrderWithUserResponseDto>> getUserOrders(
      @RequestHeader("X-User-Id") Long userId) {
    return ResponseEntity.status(HttpStatus.OK).body(createOrderWithUserResponse(userId));
  }

  private List<OrderWithUserResponseDto> createOrderWithUserResponse(Long userId) {
    List<OrderResponseDto> orders = orderService.getByUserId(userId);
    UserResponseDto user = userServiceClient.getUserById(orders.getFirst().getUserId());
    List<OrderWithUserResponseDto> response = new ArrayList<>();
    for (OrderResponseDto order : orders) {
      OrderWithUserResponseDto orderWithUserResponseDto = new OrderWithUserResponseDto(order, user);
      response.add(orderWithUserResponseDto);
    }
    return response;
  }
}
