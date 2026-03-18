package com.innowise.orderservice.model.entity;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {
  PENDING, PAID, SHIPPED, DELIVERED, CANCELLED;

  private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
      PENDING, Set.of(PAID, SHIPPED, DELIVERED, CANCELLED),
      PAID, Set.of(SHIPPED, DELIVERED, CANCELLED),
      SHIPPED, Set.of(DELIVERED, CANCELLED),
      DELIVERED, Set.of(CANCELLED),
      CANCELLED, Set.of()
  );

  public boolean allowsTransitionTo(OrderStatus nextStatus) {
    return ALLOWED_TRANSITIONS.get(this).contains(nextStatus);
  }
}