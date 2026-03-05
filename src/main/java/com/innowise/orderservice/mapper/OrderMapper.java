package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.request.OrderCreateDto;
import com.innowise.orderservice.model.dto.response.OrderItemResponseDto;
import com.innowise.orderservice.model.dto.response.OrderResponseDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import java.util.List;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "totalPrice", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "deleted", ignore = true)
  @Mapping(target = "orderItems", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Order toEntity(OrderCreateDto dto);

  @Named("orderToDto")
  @Mapping(target = "items", source = "orderItems")
  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "updatedAt", source = "updatedAt")
  OrderResponseDto toDto(Order order);

  @Named("orderItemToDto")
  @Mapping(target = "itemId", source = "item.id")
  @Mapping(target = "itemName", source = "item.name")
  @Mapping(target = "itemPrice", source = "item.price")
  @Mapping(target = "orderId", source = "order.id")
  OrderItemResponseDto toDto(OrderItem entity);

  @IterableMapping(qualifiedByName = "orderItemToDto")
  List<OrderItemResponseDto> toOrderItemDtoList(List<OrderItem> orderItems);

}