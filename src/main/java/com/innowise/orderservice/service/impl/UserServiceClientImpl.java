package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.model.dto.response.UserResponseDto;
import com.innowise.orderservice.service.UserServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

  private final UserClient userClient;

  @Override
  @CircuitBreaker(name = "userService", fallbackMethod = "userFallbackByEmail")
  public UserResponseDto getUserByEmail(String email) {
    return userClient.getUserByEmail(email);
  }

  @Override
  @CircuitBreaker(name = "userService", fallbackMethod = "userFallbackById")
  public UserResponseDto getUserById(Long id) {
    return userClient.getUserById(id);
  }

  private UserResponseDto userFallbackByEmail(String email, Throwable t) {
    return null;
  }

  private UserResponseDto userFallbackById(Long id, Throwable t) {
    return null;
  }
}