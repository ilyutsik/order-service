package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
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
  public ResponseEntity<OrderResponseDto> create(@RequestHeader("X-User-Id") Long userId,
      @Valid @RequestBody OrderCreateDto request) {
    OrderResponseDto response = orderService.create(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponseDto> getById(@PathVariable("id") Long id) {
    OrderResponseDto response = orderService.getById(id);
    UserResponseDto userInfo = userServiceClient.getUserById(response.getUserId());
    response.setUser(userInfo);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping
  public ResponseEntity<Page<OrderResponseDto>> get(@RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @RequestParam(required = false) List<String> statuses) {

    List<OrderStatus> statusEnums = null;
    if (statuses != null && !statuses.isEmpty()) {
      statusEnums = statuses.stream().map(String::toUpperCase).map(OrderStatus::valueOf).toList();
    }

    Page<OrderResponseDto> response = orderService.get(page, size, from, to, statusEnums);

    Page<OrderResponseDto> responseWithUsers = response.map(order -> {
      if (order.getUserId() != null) {
        try {
          UserResponseDto userInfo = userServiceClient.getUserById(order.getUserId());
          order.setUser(userInfo);
        } catch (Exception e) {
          order.setUser(null);
        }
      }
      return order;
    });

    return ResponseEntity.status(HttpStatus.OK).body(responseWithUsers);
  }

  @PutMapping("/{id}")
  public ResponseEntity<OrderResponseDto> update(@PathVariable("id") Long id,
      @Valid @RequestBody OrderUpdateDto updateDto) {
    OrderResponseDto response = orderService.updateById(id, updateDto);
    UserResponseDto userInfo = userServiceClient.getUserById(response.getUserId());
    response.setUser(userInfo);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    orderService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
