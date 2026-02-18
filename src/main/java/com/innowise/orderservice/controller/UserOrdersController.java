package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.dto.response.OrderWithUserResponseDto;
import com.innowise.orderservice.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserOrdersController {

  private final OrderService orderService;

  @GetMapping("/{userId}/orders")
  public ResponseEntity<List<OrderWithUserResponseDto>> getOrdersByUserId(
      @PathVariable("userId") Long userId) {
    List<OrderWithUserResponseDto> response = orderService.getByUserId(userId);

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
