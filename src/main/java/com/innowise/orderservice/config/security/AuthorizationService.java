package com.innowise.orderservice.config.security;

import com.innowise.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthorizationService {

  private final OrderRepository orderRepository;

  public boolean isOwner(Long userId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getName().equals(userId.toString());
  }

  public boolean isOrderOwner(Long orderId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    Long currentUserId = Long.parseLong(auth.getName());

    Long ownerId = orderRepository.findUserIdById(orderId);
    return ownerId != null && ownerId.equals(currentUserId);
  }

}
