package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.dto.request.ItemCreateDto;
import com.innowise.orderservice.model.dto.request.ItemUpdateDto;
import com.innowise.orderservice.model.dto.response.ItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

  private final ItemRepository itemRepository;
  private final ItemMapper itemMapper;

  @Override
  public ItemResponseDto create(ItemCreateDto itemDto) {
    Item item = itemMapper.toEntity(itemDto);
    return itemMapper.toDto(itemRepository.save(item));
  }

  @Override
  @Transactional(readOnly = true)
  public ItemResponseDto getById(Long id) {
    Item item = itemRepository.findById(id)
        .orElseThrow(() -> new ItemNotFoundException("id", id.toString()));

    return itemMapper.toDto(item);
  }

  @Override
  public ItemResponseDto updateById(Long id, ItemUpdateDto updateDto) {
    Item updatedItem = itemRepository.findById(id)
        .orElseThrow(() -> new ItemNotFoundException("id", id.toString()));

    updatedItem.setName(updateDto.getName());
    updatedItem.setPrice(updateDto.getPrice());
    return itemMapper.toDto(itemRepository.save(updatedItem));
  }

  @Override
  public void deleteById(Long id) {
    Item item = itemRepository.findById(id)
        .orElseThrow(() -> new ItemNotFoundException("id", id.toString()));

    itemRepository.delete(item);
  }
}
