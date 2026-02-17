package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.OrderWithUserResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.service.UserServiceClient;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final UserServiceClient userServiceClient;

  @PostMapping
  public ResponseEntity<OrderWithUserResponseDto> create(@RequestHeader("X-User-Id") Long userId,
      @Valid @RequestBody OrderCreateDto request) {
    OrderResponseDto order = orderService.create(request, userId);
    UserResponseDto user = userServiceClient.getUserById(userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new OrderWithUserResponseDto(order, user));
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderWithUserResponseDto> getById(@PathVariable("id") Long id) {
    OrderResponseDto order = orderService.getById(id);
    UserResponseDto user = userServiceClient.getUserById(order.getUserId());
    return ResponseEntity.status(HttpStatus.OK).body(new OrderWithUserResponseDto(order, user));
  }

  @GetMapping
  public ResponseEntity<Page<OrderWithUserResponseDto>> get(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @RequestParam(required = false) List<String> statuses) {

    List<OrderStatus> statusEnums = getOrderStatuses(statuses);

    Page<OrderResponseDto> ordersPage = orderService.get(page, size, from, to, statusEnums);

    Page<OrderWithUserResponseDto> responseWithUsers = ordersPage.map(order -> {
      UserResponseDto user = userServiceClient.getUserById(order.getUserId());
      return new OrderWithUserResponseDto(order, user);
    });

    return ResponseEntity.status(HttpStatus.OK).body(responseWithUsers);
  }

  @PutMapping("/{id}")
  public ResponseEntity<OrderWithUserResponseDto> update(@PathVariable("id") Long id,
      @Valid @RequestBody OrderUpdateDto updateDto) {
    OrderResponseDto order = orderService.updateById(id, updateDto);
    UserResponseDto user = userServiceClient.getUserById(order.getUserId());
    return ResponseEntity.status(HttpStatus.OK).body(new OrderWithUserResponseDto(order, user));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    orderService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private static List<OrderStatus> getOrderStatuses(List<String> statuses) {
    List<OrderStatus> statusEnums = null;
    if (statuses != null && !statuses.isEmpty()) {
      statusEnums = statuses.stream().map(String::toUpperCase).map(OrderStatus::valueOf).toList();
    }
    return statusEnums;
  }
}
