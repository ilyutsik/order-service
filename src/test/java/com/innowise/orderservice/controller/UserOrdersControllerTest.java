package com.innowise.orderservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.IntegrationTestBase;
import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class UserOrdersControllerTest extends IntegrationTestBase {

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

  private Order testOrder;

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();

    testOrder = new Order();
    testOrder.setUserId(1L);
    testOrder.setStatus(OrderStatus.PENDING);
    testOrder.setTotalPrice(BigDecimal.TEN);
    testOrder.setDeleted(false);
    testOrder = orderRepository.save(testOrder);
  }

  @Test
  void getByUserId_whenUserHasOrders_shouldReturnOrders() throws Exception {
    Mockito.when(userClient.getUserById(1L))
        .thenReturn(new UserResponseDto(1L, "Andrei", "ilyutsik", LocalDate.now(),
            "ilyutsik.andrei@gmail.com", true));

    mockMvc.perform(get("/api/v1/user/{userId}/orders", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].userId").value(1))
        .andExpect(jsonPath("$[0].status").value("PENDING"))
        .andExpect(jsonPath("$[0].user.name").value("Andrei"));
  }

  @Test
  void getByUserId_whenUserNotExist_shouldThrowUserNotFound() throws Exception {
    long nonExistentUserId = 999L;
    mockMvc.perform(get("/api/v1/user/{userId}/orders", nonExistentUserId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("User Not Found"));
  }

  @Test
  void getUserOrders_whenUserHasOrders_shouldReturnOrders() throws Exception {
    Mockito.when(userClient.getUserById(1L))
        .thenReturn(new UserResponseDto(1L, "Andrei", "ilyutsik", LocalDate.now(),
            "ilyutsik.andrei@gmail.com", true));

    mockMvc.perform(get("/api/v1/user/orders")
            .header("X-User-Id", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].userId").value(1))
        .andExpect(jsonPath("$[0].status").value("PENDING"))
        .andExpect(jsonPath("$[0].user.name").value("Andrei"));
  }

  @Test
  void getUserOrders_whenUserNotExist_shouldReturn404() throws Exception {
    long nonExistentUserId = 999L;
    mockMvc.perform(get("/api/v1/user/orders")
            .header("X-User-Id", nonExistentUserId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("User Not Found"));
  }
}
