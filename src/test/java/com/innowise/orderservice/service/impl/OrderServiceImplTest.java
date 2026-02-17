package com.innowise.orderservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.UserNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.request.OrderItemCreateDto;
import com.innowise.orderservice.model.dto.request.OrderUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.UserServiceClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @InjectMocks
  private OrderServiceImpl orderService;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ItemRepository itemRepository;

  @Mock
  private UserServiceClient userServiceClient;

  @Spy
  private OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

  private OrderCreateDto orderCreateDto;

  private Item item;
  private Order order;
  private UserResponseDto userResponse;

  private OrderUpdateDto updateDto;

  @BeforeEach
  void setUp() {
    orderCreateDto = new OrderCreateDto();
    OrderItemCreateDto orderItemCreateDto = new OrderItemCreateDto(1L, 4);
    orderCreateDto.setItems(List.of(orderItemCreateDto));

    orderItemCreateDto.setItemId(1L);
    orderItemCreateDto.setQuantity(4);

    order = new Order();
    order.setId(1L);
    order.setStatus(OrderStatus.PENDING);
    order.setTotalPrice(BigDecimal.valueOf(100));
    order.setUserId(1L);

    item = new Item();
    item.setId(1L);
    item.setPrice(BigDecimal.valueOf(200));

    userResponse = new UserResponseDto();
    userResponse.setId(1L);
    userResponse.setName("John");

    updateDto = new OrderUpdateDto();
    updateDto.setItems(List.of(new OrderItemCreateDto(1L, 2)));
  }

  @Test
  void create_shouldReturnOrderResponseDto_whenValidRequest() {
    when(userServiceClient.getUserById(1L)).thenReturn(userResponse);
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
    when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

    OrderResponseDto result = orderService.create(orderCreateDto, 1L);

    assertNotNull(result);
    assertEquals(BigDecimal.valueOf(800), result.getTotalPrice());

    verify(orderMapper).toEntity(orderCreateDto);
    verify(orderMapper).toDto(any(Order.class));
    verify(orderRepository, times(1)).save(any(Order.class));
  }

  @Test
  void create_userNotFound() {
    when(userServiceClient.getUserById(1L)).thenReturn(null);

    assertThrows(UserNotFoundException.class,
        () -> orderService.create(orderCreateDto, 1L));

    verify(userServiceClient, times(1)).getUserById(1L);
  }

  @Test
  void create_itemNotFound() {
    when(userServiceClient.getUserById(1L)).thenReturn(userResponse);

    when(itemRepository.findById(any(Long.class))).thenReturn(Optional.empty());

    assertThrows(ItemNotFoundException.class,
        () -> orderService.create(orderCreateDto, 1L));

    verify(itemRepository, times(1)).findById(any(Long.class));
  }

  @Test
  void getById_shouldReturnOrderResponseDto_whenOrderExists() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    OrderResponseDto result = orderService.getById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals(OrderStatus.PENDING, result.getStatus());
    assertEquals(BigDecimal.valueOf(100), result.getTotalPrice());
    assertEquals(1L, result.getUserId());

    verify(orderMapper).toDto(order);
    verify(orderRepository).findById(1L);
  }

  @Test
  void getById_shouldThrowOrderNotFoundException_whenOrderDoesNotExist() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class,
        () -> orderService.getById(1L));

    verify(orderRepository, times(1)).findById(1L);
    verifyNoInteractions(orderMapper);
  }

  @Test
  void get_shouldReturnPageOfOrderResponseDto_whenOrdersExist() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Order> ordersPage = new PageImpl<>(List.of(order), pageable, 1);

    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(ordersPage);

    Page<OrderResponseDto> result = orderService.get(0, 10, LocalDateTime.now().minusDays(1),
        LocalDateTime.now(), List.of(OrderStatus.PENDING));

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(order.getId(), result.getContent().get(0).getId());
    assertEquals(order.getStatus(), result.getContent().get(0).getStatus());

    verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    verify(orderMapper).toDto(order);
  }

  @Test
  void get_shouldReturnEmptyPage_whenNoOrdersExist() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Order> emptyPage = new PageImpl<>(List.of(), pageable, 0);
    when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

    Page<OrderResponseDto> result = orderService.get(0, 10, LocalDateTime.now().minusDays(1),
        LocalDateTime.now(), List.of(OrderStatus.PENDING));

    assertNotNull(result);
    assertTrue(result.getContent().isEmpty());
    assertEquals(0, result.getTotalElements());

    verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    verifyNoInteractions(orderMapper);
  }

  @Test
  void getByUserId_shouldReturnOrderResponseDto_whenUserExists() {
    when(orderRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(List.of(order));

    List<OrderResponseDto> result = orderService.getByUserId(1L);

    assertNotNull(result);
    assertEquals(1L, result.getFirst().getId());
    assertEquals(OrderStatus.PENDING, result.getFirst().getStatus());
    assertEquals(BigDecimal.valueOf(100), result.getFirst().getTotalPrice());
    assertEquals(1L, result.getFirst().getUserId());

    verify(orderMapper).toDto(order);
    verify(orderRepository).findByUserIdAndDeletedFalse(1L);
  }

  @Test
  void getByUserId_shouldThrowOrderNotFoundException_whenUserDoesNotExist() {
    when(orderRepository.findByUserIdAndDeletedFalse(1L)).thenReturn(List.of());

    assertThrows(UserNotFoundException.class,
        () -> orderService.getByUserId(1L));

    verify(orderRepository, times(1)).findByUserIdAndDeletedFalse(1L);
    verifyNoInteractions(orderMapper);
  }

  @Test
  void updateById_shouldUpdateOrder_whenOrderExists() {
    order.setOrderItems(new ArrayList<>());

    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

    OrderResponseDto result = orderService.updateById(order.getId(), updateDto);

    assertNotNull(result);
    assertEquals(order.getId(), result.getId());
    assertNotNull(result.getItems());
    assertEquals(1, result.getItems().size());
    assertEquals(2, result.getItems().get(0).getQuantity());
    assertEquals(order.getUserId(), result.getUserId());

    verify(orderRepository).findById(order.getId());
    verify(orderRepository).save(order);
  }

  @Test
  void updateById_shouldThrowOrderNotFoundException_whenOrderDoesNotExist() {
    Long orderId = 999L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

   assertThrows(OrderNotFoundException.class,
        () -> orderService.updateById(orderId, updateDto));


    verify(orderRepository, times(1)).findById(orderId);
    verify(orderRepository, never()).save(any());
  }

  @Test
  void updateById_shouldThrowItemNotFoundException_whenItemDoesNotExist() {
    order.setOrderItems(List.of(
        new OrderItem() {{
          setItem(new Item() {{ setId(1L); setPrice(BigDecimal.valueOf(100)); }});
          setQuantity(1);
          setOrder(order);
        }}
    ));
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    when(itemRepository.findById(1L)).thenReturn(Optional.empty());

    Long orderId = order.getId();
    assertThrows(ItemNotFoundException.class,
        () -> orderService.updateById(orderId, updateDto));

    verify(orderRepository, times(1)).findById(order.getId());
    verify(orderRepository, never()).save(any());
  }

  @Test
  void deleteById_shouldReturnVoid_whenOrderExists() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    orderService.deleteById(1L);

    verify(orderRepository).findById(1L);
    verify(orderRepository).delete(any(Order.class));
  }

  @Test
  void deleteById_shouldThrowOrderNotFoundException_whenOrderDoesNotExist() {
    when(orderRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class,
        () -> orderService.deleteById(1L));

    verify(orderRepository, times(1)).findById(1L);
    verify(orderRepository, times(0)).delete(any(Order.class));
  }
}