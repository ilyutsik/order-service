package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSpecifications {

  private static final String CREATED_AT = "createdAt";

  public static Specification<Order> createdBetween(LocalDateTime from, LocalDateTime to) {
    return (root, query, cb) -> {
      if (from != null && to != null) {
        return cb.between(root.get(CREATED_AT), from, to);
      } else if (from != null) {
        return cb.greaterThanOrEqualTo(root.get(CREATED_AT), from);
      } else if (to != null) {
        return cb.lessThanOrEqualTo(root.get(CREATED_AT), to);
      } else {
        return cb.conjunction();
      }
    };
  }

  public static Specification<Order> hasStatus(List<OrderStatus> statuses) {
    return (root, query, cb) -> statuses == null || statuses.isEmpty()
        ? cb.conjunction()
        : root.get("status").in(statuses);
  }
}