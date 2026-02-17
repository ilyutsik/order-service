package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.UserNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
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
  public OrderResponseDto create(OrderCreateDto dto, Long userId) {
    Order order = orderMapper.toEntity(dto);

    UserResponseDto userResponse = userServiceClient.getUserById(userId);
    if (userResponse == null) {
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

    return orderMapper.toDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponseDto getById(Long id) {
    return orderMapper.toDto(orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("id", id.toString())));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponseDto> get(int page, int size, LocalDateTime from, LocalDateTime to,
      List<OrderStatus> statuses) {
    Specification<Order> spec = OrderSpecifications.notDeleted()
        .and(OrderSpecifications.createdBetween(from, to))
        .and(OrderSpecifications.hasStatus(statuses));

    Pageable pageable = PageRequest.of(page, size);
    return orderRepository.findAll(spec, pageable).map(orderMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderResponseDto> getByUserId(Long id) {
    List<Order> order = orderRepository.findByUserIdAndDeletedFalse(id);
    if (order.isEmpty()) {
      throw new UserNotFoundException("id", id.toString());
    }
    return orderMapper.toOrderDtoList(order);
  }

  @Override
  public OrderResponseDto updateById(Long id, OrderUpdateDto updateDto) {
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
    return orderMapper.toDto(updated);
  }

  @Override
  public void deleteById(Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

    orderRepository.delete(order);
  }
}
