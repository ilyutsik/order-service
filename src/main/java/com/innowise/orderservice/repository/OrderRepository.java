package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  Page<Order> findAll(Specification<Order> spec, Pageable pageable);

  List<Order> findByUserId(Long userId);
}
