package com.innowise.orderservice.controller;

import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.request.OrderItemUpdateDto;
import com.innowise.orderservice.model.dto.request.OrderStatusUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderWithUserResponseDto;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final UserServiceClient userServiceClient;

  @PostMapping
  public ResponseEntity<OrderWithUserResponseDto> create(@RequestHeader("X-User-Id") Long userId,
      @Valid @RequestBody OrderCreateDto request) {
    OrderWithUserResponseDto response = orderService.create(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderWithUserResponseDto> getById(@PathVariable("id") Long id) {
    OrderWithUserResponseDto response = orderService.getById(id);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping
  public ResponseEntity<Page<OrderWithUserResponseDto>> get(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @RequestParam(required = false) List<OrderStatus> statuses) {

    Page<OrderWithUserResponseDto> response = orderService.get(page, size, from, to, statuses);

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PatchMapping("/{id}/items")
  public ResponseEntity<OrderWithUserResponseDto> updateItems(@PathVariable("id") Long id,
      @Valid @RequestBody OrderItemUpdateDto updateDto) {
    OrderWithUserResponseDto response = orderService.updateItemById(id, updateDto);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderWithUserResponseDto> updateStatus(@PathVariable("id") Long id,
      @Valid @RequestBody OrderStatusUpdateDto updateDto) {
    OrderWithUserResponseDto response = orderService.updateStatusById(id, updateDto);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    orderService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
