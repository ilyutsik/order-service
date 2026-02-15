package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.service.UserServiceClient;
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
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserOrdersController {

  private final OrderService orderService;
  private final UserServiceClient userServiceClient;

  @GetMapping("/{userId}/orders")
  public ResponseEntity<List<OrderResponseDto>> getByUserId(@PathVariable("userId") Long userId) {
    return ResponseEntity.status(HttpStatus.OK).body(createOrderResponse(userId));
  }

  @GetMapping("/orders")
  public ResponseEntity<List<OrderResponseDto>> getUserOrders(
      @RequestHeader("X-User-Id") Long userId) {
    return ResponseEntity.status(HttpStatus.OK).body(createOrderResponse(userId));
  }

  private List<OrderResponseDto> createOrderResponse(Long userId) {
    List<OrderResponseDto> response = orderService.getByUserId(userId);
    return createResponseWithUsers(response);
  }

  private List<OrderResponseDto> createResponseWithUsers(List<OrderResponseDto> response) {
    return response.stream().map(order -> {
      if (order.getUserId() != null) {
        try {
          UserResponseDto userInfo = userServiceClient.getUserById(order.getUserId());
          order.setUser(userInfo);
        } catch (Exception e) {
          order.setUser(null);
        }
      }
      return order;
    }).toList();
  }
}
