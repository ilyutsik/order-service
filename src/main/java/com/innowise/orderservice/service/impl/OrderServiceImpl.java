package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.UserNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.request.OrderItemUpdateDto;
import com.innowise.orderservice.model.dto.request.OrderStatusUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.OrderWithUserResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.repository.OrderSpecifications;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.service.UserServiceClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final UserServiceClient userServiceClient;
  private final OrderRepository orderRepository;
  private final ItemRepository itemRepository;
  private final OrderMapper orderMapper;

  @Override
  public OrderWithUserResponseDto create(OrderCreateDto dto, Long userId) {
    Order order = orderMapper.toEntity(dto);

    UserResponseDto userDto = userServiceClient.getUserById(userId);
    if (userDto == null) {
      throw new UserNotFoundException("id", userId.toString());
    }

    order.setUserId(userId);
    order.setStatus(OrderStatus.PENDING);

    List<OrderItem> items = dto.getItems().stream().map(i -> {
      Item item = itemRepository.findById(i.getItemId())
          .orElseThrow(() -> new ItemNotFoundException("id", i.getItemId().toString()));
      OrderItem oi = new OrderItem();
      oi.setItem(item);
      oi.setQuantity(i.getQuantity());
      oi.setOrder(order);
      return oi;
    }).toList();

    order.setOrderItems(items);

    order.setTotalPrice(items.stream()
        .map(oi -> oi.getItem().getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add));

    Order saved = orderRepository.save(order);

    OrderResponseDto orderDto = orderMapper.toDto(saved);
    return new OrderWithUserResponseDto(orderDto, userDto);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderWithUserResponseDto getById(Long id) {
    OrderResponseDto orderDto = orderMapper.toDto(orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("id", id.toString())));

    UserResponseDto userDto = userServiceClient.getUserById(orderDto.getUserId());

    return new OrderWithUserResponseDto(orderDto, userDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderWithUserResponseDto> get(int page, int size, LocalDateTime from,
      LocalDateTime to,
      List<OrderStatus> statuses) {
    Specification<Order> spec = OrderSpecifications.hasStatus(statuses)
        .and(OrderSpecifications.createdBetween(from, to));

    Pageable pageable = PageRequest.of(page, size);
    Page<OrderResponseDto> ordersPage = orderRepository.findAll(spec, pageable)
        .map(orderMapper::toDto);

    return ordersPage.map(order -> {
      UserResponseDto userDto = userServiceClient.getUserById(order.getUserId());
      return new OrderWithUserResponseDto(order, userDto);
    });
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderWithUserResponseDto> getByUserId(Long userId) {
    UserResponseDto userDto = userServiceClient.getUserById(userId);
    if (userDto == null) {
      throw new UserNotFoundException("id", userId.toString());
    }

    List<Order> orders = orderRepository.findByUserId(userId);

    List<OrderWithUserResponseDto> response = new ArrayList<>();
    for (Order order : orders) {
      OrderWithUserResponseDto orderWithUserResponseDto = new OrderWithUserResponseDto(
          orderMapper.toDto(order),
          userDto);
      response.add(orderWithUserResponseDto);
    }

    if (response.isEmpty()) {
      response.add(new OrderWithUserResponseDto(null, userDto));
    }
    
    return response;
  }

  @Override
  public OrderWithUserResponseDto updateItemById(Long id, OrderItemUpdateDto updateDto) {
    Order updatedOrder = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

    List<OrderItem> newItems = updateDto.getItems().stream().map(dtoItem -> {
      Item item = itemRepository.findById(dtoItem.getItemId())
          .orElseThrow(() -> new ItemNotFoundException("id", dtoItem.getItemId().toString()));
      OrderItem orderItem = new OrderItem();
      orderItem.setOrder(updatedOrder);
      orderItem.setItem(item);
      orderItem.setQuantity(dtoItem.getQuantity());
      return orderItem;
    }).toList();

    updatedOrder.getOrderItems().clear();
    updatedOrder.getOrderItems().addAll(newItems);

    updatedOrder.setTotalPrice(newItems.stream()
        .map(oi -> oi.getItem().getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add));

    Order updated = orderRepository.save(updatedOrder);
    UserResponseDto userDto = userServiceClient.getUserById(updated.getUserId());
    return new OrderWithUserResponseDto(orderMapper.toDto(updated), userDto);
  }

  @Override
  public OrderWithUserResponseDto updateStatusById(Long id, OrderStatusUpdateDto updateDto) {
    Order updatedOrder = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

    updatedOrder.setStatus(updateDto.getStatus());

    Order updated = orderRepository.save(updatedOrder);
    UserResponseDto userDto = userServiceClient.getUserById(updated.getUserId());
    return new OrderWithUserResponseDto(orderMapper.toDto(updated), userDto);
  }

  @Override
  public void deleteById(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

    orderRepository.delete(order);
  }
}
