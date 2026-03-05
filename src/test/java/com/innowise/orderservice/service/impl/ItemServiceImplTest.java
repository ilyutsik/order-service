package com.innowise.orderservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.dto.request.ItemCreateDto;
import com.innowise.orderservice.model.dto.request.ItemUpdateDto;
import com.innowise.orderservice.model.dto.response.ItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

  @InjectMocks
  private ItemServiceImpl itemService;

  @Mock
  private ItemRepository itemRepository;

  @Spy
  private ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

  private ItemCreateDto createDto;

  private ItemResponseDto responseDto;

  private Item item;

  @BeforeEach
  void setUp() {
    createDto = new ItemCreateDto();
    createDto.setName("car");
    createDto.setPrice(BigDecimal.valueOf(700));

    responseDto = new ItemResponseDto();
    responseDto.setName("car");
    responseDto.setPrice(BigDecimal.valueOf(700));

    item = new Item();
    item.setId(1L);
    item.setName("car");
    item.setPrice(BigDecimal.valueOf(700));
  }

  @Test
  void create_shouldReturnItemResponseDto_whenCreateDtoIsValid() {
    when(itemRepository.save(any(Item.class))).thenAnswer(i -> i.getArguments()[0]);

    ItemResponseDto response = itemService.create(createDto);

    assertEquals(responseDto.getName(), response.getName());
    assertEquals(responseDto.getPrice(), response.getPrice());
  }

  @Test
  void getById_shouldReturnItemResponseDto_whenItemExists() {
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

    ItemResponseDto result = itemService.getById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("car", result.getName());
    assertEquals(BigDecimal.valueOf(700), result.getPrice());
  }

  @Test
  void getById_shouldThrowException_whenItemNotFound() {
    when(itemRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ItemNotFoundException.class, () -> itemService.getById(1L));

    verify(itemRepository).findById(1L);
  }

  @Test
  void updateById_shouldUpdateItem_whenItemExists() {
    Long id = 1L;

    ItemUpdateDto updateDto = new ItemUpdateDto();
    updateDto.setName("bike");
    updateDto.setPrice(BigDecimal.valueOf(900));

    when(itemRepository.findById(id)).thenReturn(Optional.of(item));
    when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ItemResponseDto result = itemService.updateById(id, updateDto);

    assertEquals("bike", result.getName());
    assertEquals(BigDecimal.valueOf(900), result.getPrice());

    verify(itemRepository).findById(id);
    verify(itemRepository).save(item);
  }

  @Test
  void updateById_shouldThrowException_whenItemNotFound() {
    Long id = 1L;

    ItemUpdateDto updateDto = new ItemUpdateDto();
    updateDto.setName("bike");
    updateDto.setPrice(BigDecimal.valueOf(900));

    when(itemRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ItemNotFoundException.class, () -> itemService.updateById(id, updateDto));

    verify(itemRepository).findById(id);
    verify(itemRepository, never()).save(any());
  }

  @Test
  void deleteById_shouldDeleteItem_whenItemExists() {
    Long id = 1L;
    when(itemRepository.findById(id)).thenReturn(Optional.of(item));

    itemService.deleteById(id);

    verify(itemRepository).findById(id);
    verify(itemRepository).delete(item);
  }

  @Test
  void deleteById_shouldThrowException_whenItemNotFound() {
    Long id = 1L;
    when(itemRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ItemNotFoundException.class, () -> itemService.deleteById(id));

    verify(itemRepository).findById(id);
    verify(itemRepository, never()).delete(any());
  }
}
