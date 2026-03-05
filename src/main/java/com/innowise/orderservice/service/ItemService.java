package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.request.ItemCreateDto;
import com.innowise.orderservice.model.dto.request.ItemUpdateDto;
import com.innowise.orderservice.model.dto.response.ItemResponseDto;

public interface ItemService {

  ItemResponseDto create(ItemCreateDto itemDto);

  ItemResponseDto getById(Long id);

  ItemResponseDto updateById(Long id, ItemUpdateDto updateDto);

  void deleteById(Long id);

}
