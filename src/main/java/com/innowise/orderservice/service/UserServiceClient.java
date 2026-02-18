package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.response.UserResponseDto;

public interface UserServiceClient {

  UserResponseDto getUserByEmail(String email);

  UserResponseDto getUserById(Long id);
}
