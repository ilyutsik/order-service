package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.response.UserResponseDto;

public interface UserServiceClient {

  UserResponseDto getUserById(Long id);
}
