package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.request.ItemCreateDto;
import com.innowise.orderservice.model.dto.request.ItemUpdateDto;
import com.innowise.orderservice.model.dto.response.ItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {


  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Item toEntity(ItemCreateDto createDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Item toEntity(ItemUpdateDto updateDto);


  ItemResponseDto toDto(Item item);
}
