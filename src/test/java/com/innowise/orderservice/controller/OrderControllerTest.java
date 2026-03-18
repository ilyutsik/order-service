package com.innowise.orderservice.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.orderservice.IntegrationTestBase;
import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.request.OrderItemCreateDto;
import com.innowise.orderservice.model.dto.request.OrderItemUpdateDto;
import com.innowise.orderservice.model.dto.response.OrderWithUserResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class OrderControllerTest extends IntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private OrderRepository orderRepository;

  private OrderCreateDto testOrderCreateDto;

  private Item testItem;

  private Item newItem;

  private Order testOrder;

  @BeforeEach
  @Commit
  void setUp() {
    WireMock.configureFor(wiremock.getHost(), wiremock.getMappedPort(8080));
    WireMock.reset();

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

    List<OrderItem> items = new ArrayList<>();
    items.add(orderItem);
    testOrder.setOrderItems(items);
    testOrder.setTotalPrice(testItem.getPrice().multiply(BigDecimal.valueOf(2)));

    testOrder = orderRepository.save(testOrder);

    testOrderCreateDto = new OrderCreateDto();
    testOrderCreateDto.setItems(List.of(new OrderItemCreateDto(testItem.getId(), 2)));
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
  void create_whenValidUser_shouldReturnCreated() throws Exception {
    stubUser(1L);

    MvcResult result = mockMvc.perform(
            post("/api/v1/orders").header("X-USER-ID", String.valueOf(1L))
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderCreateDto)))
        .andExpect(status().isCreated()).andReturn();

    OrderWithUserResponseDto response = objectMapper.readValue(
        result.getResponse().getContentAsString(), OrderWithUserResponseDto.class);

    assertThat(response.getUser().getName()).isEqualTo("Andrei");
    assertThat(response.getOrder().getTotalPrice()).isNotNull();
  }

  @Test
  void create_whenUserNotFound_shouldReturn404() throws Exception {
    stubUser(1L);

    mockMvc.perform(
            post("/api/v1/orders").header("X-User-Id", 99L)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderCreateDto)))
        .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("User Not Found"));
  }

  @Test
  void create_whenItemNotFound_shouldReturn404() throws Exception {
    stubUser(1L);

    itemRepository.deleteById(testItem.getId());

    mockMvc.perform(
            post("/api/v1/orders").header("X-User-Id", 1L)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderCreateDto)))
        .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("Item Not Found"));
  }

  @Test
  void create_whenInvalidRequest_shouldReturn400() throws Exception {
    stubUser(1L);

    testOrderCreateDto.setItems(Collections.emptyList());

    mockMvc.perform(
            post("/api/v1/orders").header("X-User-Id", 1L)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderCreateDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Validation Failed"));
  }

  @Test
  void getById_whenOrderExistsAndUserExists_shouldReturn200() throws Exception {
    stubUser(1L);

    mockMvc.perform(get("/api/v1/orders/{id}", testOrder.getId())
            .with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user.name").value("Andrei"))
        .andExpect(jsonPath("$.order.totalPrice").value(testOrder.getTotalPrice().intValue()));
  }

  @Test
  void getById_whenOrderNotFound_shouldReturn404() throws Exception {
    mockMvc.perform(get("/api/v1/orders/{id}", 999L)
            .with(user("admin").roles("ADMIN")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order Not Found"));
  }

  @Test
  void getOrders_whenOrdersExist_shouldReturnPageWithUsers() throws Exception {
    stubUser(1L);

    mockMvc.perform(
            get("/api/v1/orders").param("page", "0").param("size", "10")
                .param("statuses", "PENDING")
                .with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].user.name").value("Andrei"))
        .andExpect(
            jsonPath("$.content[0].order.totalPrice").value(testOrder.getTotalPrice().intValue()));
  }

  @Test
  void getOrders_whenNoOrders_shouldReturnEmptyPage() throws Exception {
    orderRepository.deleteAll();
    itemRepository.deleteAll();

    mockMvc.perform(get("/api/v1/orders").param("page", "0")
            .param("size", "10")
            .with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void getOrders_whenStatusesFilterEmpty_shouldReturnAllOrders() throws Exception {
    stubUser(1L);

    mockMvc.perform(get("/api/v1/orders").param("page", "0")
            .param("size", "10")
            .with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].user.name").value("Andrei"));
  }

  @Test
  void updateOrder_whenValidRequest_shouldReturnUpdatedOrder() throws Exception {
    stubUser(1L);

    OrderItemUpdateDto updateDto = new OrderItemUpdateDto();
    updateDto.setItems(List.of(new OrderItemCreateDto(newItem.getId(), 3)));

    MvcResult result = mockMvc.perform(
            patch("/api/v1/orders/{id}/items", testOrder.getId())
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk()).andReturn();

    OrderWithUserResponseDto response = objectMapper.readValue(
        result.getResponse().getContentAsString(), OrderWithUserResponseDto.class);

    assertThat(response.getUser().getName()).isEqualTo("Andrei");
    assertThat(response.getOrder().getTotalPrice()).isEqualByComparingTo(
        newItem.getPrice().multiply(BigDecimal.valueOf(3)));
    assertThat(response.getOrder().getItems().get(0).getItemId()).isEqualTo(newItem.getId());
  }

  @Test
  void updateOrder_whenOrderNotFound_shouldReturnNotFound() throws Exception {
    OrderItemUpdateDto updateDto = new OrderItemUpdateDto();
    updateDto.setItems(List.of(new OrderItemCreateDto(newItem.getId(), 1)));

    mockMvc.perform(patch("/api/v1/orders/{id}/items", 999L)
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order Not Found"));
  }

  @Test
  void updateOrder_whenItemNotFound_shouldReturnNotFound() throws Exception {
    OrderItemUpdateDto updateDto = new OrderItemUpdateDto();
    updateDto.setItems(List.of(new OrderItemCreateDto(999L, 1)));

    mockMvc.perform(patch("/api/v1/orders/{id}/items", testOrder.getId())
            .with(user("admin").roles("ADMIN"))
            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("Item Not Found"));
  }

  @Test
  void deleteOrder_whenNotExists_shouldReturnNotFound() throws Exception {
    long nonExistentId = 9999L;
    mockMvc.perform(delete("/api/v1/orders/{id}", nonExistentId)
            .with(user("admin").roles("ADMIN")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order Not Found"));
  }
}
