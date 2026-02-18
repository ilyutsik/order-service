package com.innowise.orderservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.IntegrationTestBase;
import com.innowise.orderservice.model.dto.request.ItemCreateDto;
import com.innowise.orderservice.model.dto.request.ItemUpdateDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ItemControllerTest extends IntegrationTestBase {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ItemRepository itemRepository;

  private Item testItem;

  private ItemCreateDto testItemCreateDto;

  @BeforeEach
  void setUp() {
    testItem = new Item();
    testItem.setName("car");
    testItem.setPrice(BigDecimal.TEN);
    testItem = itemRepository.save(testItem);

    testItemCreateDto = new ItemCreateDto();
    testItemCreateDto.setName("bike");
    testItemCreateDto.setPrice(BigDecimal.valueOf(15));
  }

  @Test
  void create_whenValidRequest_shouldReturnCreatedItem() throws Exception {
    mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testItemCreateDto)))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("bike"))
        .andExpect(jsonPath("$.price").value(15));
  }

  @Test
  void create_whenNameIsEmpty_shouldReturnBadRequest() throws Exception {
    ItemCreateDto request = new ItemCreateDto();
    request.setName("");
    request.setPrice(BigDecimal.valueOf(15));

    mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name").exists());
  }

  @Test
  void getById_whenItemExists_shouldReturnItem() throws Exception {
    mockMvc.perform(
            get("/api/v1/items/{id}", testItem.getId()).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(testItem.getId()))
        .andExpect(jsonPath("$.name").value(testItem.getName()))
        .andExpect(jsonPath("$.price").value(testItem.getPrice().intValue()));
  }

  @Test
  void getById_whenItemDoesNotExist_shouldReturnNotFound() throws Exception {
    Long nonExistentId = 999L;
    mockMvc.perform(
            get("/api/v1/items/{id}", nonExistentId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("Item Not Found"));
  }

  @Test
  void update_whenItemExists_shouldReturnUpdatedItem() throws Exception {
    ItemUpdateDto updateDto = new ItemUpdateDto();
    updateDto.setName("updatedCar");
    updateDto.setPrice(BigDecimal.valueOf(20));

    mockMvc.perform(
            put("/api/v1/items/{id}", testItem.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testItem.getId()))
        .andExpect(jsonPath("$.name").value("updatedCar")).andExpect(jsonPath("$.price").value(20));
  }

  @Test
  void update_whenItemDoesNotExist_shouldReturnNotFound() throws Exception {
    Long nonExistentId = 999L;
    ItemUpdateDto updateDto = new ItemUpdateDto();
    updateDto.setName("updatedCar");
    updateDto.setPrice(BigDecimal.valueOf(20));

    mockMvc.perform(put("/api/v1/items/{id}", nonExistentId).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto))).andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Item Not Found"));
  }

  @Test
  void delete_whenItemExists_shouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/v1/items/{id}", testItem.getId()))
        .andExpect(status().isNoContent());

    Optional<Item> deleted = itemRepository.findById(testItem.getId());
    assertThat(deleted).isEmpty();
  }

  @Test
  void delete_whenItemDoesNotExist_shouldReturnNotFound() throws Exception {
    Long nonExistentId = 999L;
    mockMvc.perform(delete("/api/v1/items/{id}", nonExistentId)).andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Item Not Found"));
  }
}
