package com.innowise.orderservice.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.IntegrationTestBase;
import com.innowise.orderservice.client.UserClient;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class OrderControllerTest extends IntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private OrderRepository orderRepository;

  @MockBean
  private UserClient userClient;

  private OrderCreateDto testOrderCreateDto;

  private Item testItem;

  private Item newItem;

  private Order testOrder;

  @BeforeEach
  void setUp() {
    itemRepository.deleteAll();
    orderRepository.deleteAll();

    testItem = new Item();
    testItem.setName("car");
    testItem.setPrice(BigDecimal.TEN);
    testItem = itemRepository.save(testItem);

    newItem = new Item();
    newItem.setName("bike");
    newItem.setPrice(BigDecimal.valueOf(15));
    itemRepository.save(newItem);

    testOrder = new Order();
    testOrder.setUserId(1L);
    testOrder.setStatus(OrderStatus.PENDING);
    testOrder.setTotalPrice(BigDecimal.TEN);
    testOrder.setDeleted(false);

    OrderItem orderItem = new OrderItem();
    orderItem.setItem(testItem);
    orderItem.setQuantity(2);
    orderItem.setOrder(testOrder);

    testOrder.setOrderItems(List.of(orderItem));
    testOrder.setTotalPrice(testItem.getPrice().multiply(BigDecimal.valueOf(2)));

    testOrder = orderRepository.save(testOrder);

    testOrderCreateDto = new OrderCreateDto();
    testOrderCreateDto.setItems(List.of(new OrderItemCreateDto(testItem.getId(), 2)));
  }

  @Test
  void create_whenValidUser_shouldReturnCreated() throws Exception {
    Mockito.when(userClient.getUserById(1L))
        .thenReturn(new UserResponseDto(1L, "Andrei", "ilyutsik", LocalDate.now(),
            "ilyutsik.adnrei@gmail.com", true));

    MvcResult result = mockMvc.perform(post("/api/v1/order")
            .header("X-User-Id", String.valueOf(1L))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testOrderCreateDto)))
        .andExpect(status().isCreated())
        .andReturn();

    OrderResponseDto response = objectMapper.readValue(
        result.getResponse().getContentAsString(), OrderResponseDto.class);

    assertThat(response.getUser().getName()).isEqualTo("Andrei");
    assertThat(response.getTotalPrice()).isNotNull();
  }

  @Test
  void create_whenUserNotFound_shouldReturn404() throws Exception {
    Mockito.when(userClient.getUserById(99L)).thenReturn(null);

    mockMvc.perform(post("/api/v1/order")
            .header("X-User-Id", 99L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testOrderCreateDto)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("User Not Found"));
  }

  @Test
  void create_whenItemNotFound_shouldReturn404() throws Exception {
    Mockito.when(userClient.getUserById(1L))
        .thenReturn(new UserResponseDto(1L, "Andrei", "ilyutsik", LocalDate.now(),
            "ilyutsik.adnrei@gmail.com", true));

    itemRepository.delete(testItem);

    mockMvc.perform(post("/api/v1/order")
            .header("X-User-Id", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testOrderCreateDto)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Item Not Found"));
  }

  @Test
  void create_whenInvalidRequest_shouldReturn400() throws Exception {
    Mockito.when(userClient.getUserById(1L))
        .thenReturn(new UserResponseDto(1L, "Andrei", "ilyutsik", LocalDate.now(),
            "ilyutsik.adnrei@gmail.com", true));

    testOrderCreateDto.setItems(Collections.emptyList());

    mockMvc.perform(post("/api/v1/order")
            .header("X-User-Id", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testOrderCreateDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Validation Failed"));
  }

  @Test
  void getById_whenOrderExistsAndUserExists_shouldReturn200() throws Exception {
    Mockito.when(userClient.getUserById(1L))
        .thenReturn(new UserResponseDto(1L, "Andrei", "ilyutsik", LocalDate.now(),
            "ilyutsik.adnrei@gmail.com", true));

    mockMvc.perform(get("/api/v1/order/{id}", testOrder.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.name").value("Andrei"))
        .andExpect(jsonPath("$.totalPrice").value(testOrder.getTotalPrice().intValue()));
  }

  @Test
  void getById_whenOrderNotFound_shouldReturn404() throws Exception {
    mockMvc.perform(get("/api/v1/order/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order Not Found"));
  }

  @Test
  void getOrders_whenOrdersExist_shouldReturnPageWithUsers() throws Exception {
    Mockito.when(userClient.getUserById(1L))
        .thenReturn(new UserResponseDto(1L, "Andrei", "ilyutsik",
            LocalDate.now(), "ilyutsik.adnrei@gmail.com", true));

    mockMvc.perform(get("/api/v1/order")
            .param("page", "0")
            .param("size", "10")
            .param("statuses", "PENDING"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].user.name").value("Andrei"))
        .andExpect(jsonPath("$.content[0].totalPrice").value(testOrder.getTotalPrice().intValue()));
  }

  @Test
  void getOrders_whenNoOrders_shouldReturnEmptyPage() throws Exception {
    itemRepository.deleteAll();
    orderRepository.deleteAll();

    mockMvc.perform(get("/api/v1/order")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void getOrders_whenStatusesFilterEmpty_shouldReturnAllOrders() throws Exception {
    Mockito.when(userClient.getUserById(1L))
        .thenReturn(new UserResponseDto(1L, "Andrei", "ilyutsik",
            LocalDate.now(), "ilyutsik.adnrei@gmail.com", true));

    mockMvc.perform(get("/api/v1/order")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].user.name").value("Andrei"));
  }

  @Test
  void updateOrder_whenValidRequest_shouldReturnUpdatedOrder() throws Exception {
    Mockito.when(userClient.getUserById(1L))
        .thenReturn(new UserResponseDto(1L, "Andrei", "ilyutsik",
            LocalDate.now(), "ilyutsik.adnrei@gmail.com", true));

    OrderUpdateDto updateDto = new OrderUpdateDto();
    updateDto.setItems(List.of(new OrderItemCreateDto(newItem.getId(), 3)));

    MvcResult result = mockMvc.perform(put("/api/v1/order/{id}", testOrder.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andReturn();

    OrderResponseDto response = objectMapper.readValue(
        result.getResponse().getContentAsString(), OrderResponseDto.class);

    assertThat(response.getUser().getName()).isEqualTo("Andrei");
    assertThat(response.getTotalPrice()).isEqualByComparingTo(newItem.getPrice().multiply(BigDecimal.valueOf(3)));
    assertThat(response.getItems().get(0).getItemId()).isEqualTo(newItem.getId());
  }

  @Test
  void updateOrder_whenOrderNotFound_shouldReturnNotFound() throws Exception {
    OrderUpdateDto updateDto = new OrderUpdateDto();
    updateDto.setItems(List.of(new OrderItemCreateDto(newItem.getId(), 1)));

    mockMvc.perform(put("/api/v1/order/{id}", 999L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order Not Found"));
  }

  @Test
  void updateOrder_whenItemNotFound_shouldReturnNotFound() throws Exception {
    OrderUpdateDto updateDto = new OrderUpdateDto();
    updateDto.setItems(List.of(new OrderItemCreateDto(999L, 1)));

    mockMvc.perform(put("/api/v1/order/{id}", testOrder.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Item Not Found"));
  }

  @Test
  void deleteOrder_whenExists_shouldMarkDeleted() throws Exception {
    mockMvc.perform(delete("/api/v1/order/{id}", testOrder.getId()))
        .andExpect(status().isNoContent());

    Order deletedOrder = orderRepository.findById(testOrder.getId())
        .orElseThrow();
    assertThat(deletedOrder.getDeleted()).isTrue();
  }

  @Test
  void deleteOrder_whenNotExists_shouldReturnNotFound() throws Exception {
    long nonExistentId = 9999L;
    mockMvc.perform(delete("/api/v1/order/{id}", nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order Not Found"));
  }
}
