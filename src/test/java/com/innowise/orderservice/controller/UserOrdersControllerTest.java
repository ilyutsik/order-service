package com.innowise.orderservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.orderservice.IntegrationTestBase;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserOrdersControllerTest extends IntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private OrderRepository orderRepository;

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

  @BeforeEach
  void setupWiremock() {
    WireMock.configureFor(wiremock.getHost(), wiremock.getMappedPort(8080));
    WireMock.reset();
  }

  private void stubUser(Long userId) {
    WireMock.stubFor(WireMock.get("/api/v1/users/" + userId).willReturn(
        WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/json")
            .withBody("""
                {
                  "id": %d,
                  "name": "Andrei",
                  "surname": "ilyutsik",
                  "email": "ilyutsik.adnrei@gmail.com",
                  "active" : "true"
                }
                """.formatted(userId))));
  }

  @Test
  void getByUserId_whenUserHasOrders_shouldReturnOrders() throws Exception {
    stubUser(1L);

    mockMvc.perform(get("/api/v1/user/{userId}/orders", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].order.userId").value(1))
        .andExpect(jsonPath("$[0].order.status").value("PENDING"))
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
    stubUser(1L);

    mockMvc.perform(get("/api/v1/user/orders")
            .header("X-User-Id", 1L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].order.userId").value(1))
        .andExpect(jsonPath("$[0].order.status").value("PENDING"))
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
