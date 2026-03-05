package com.innowise.orderservice.client;

import com.innowise.orderservice.model.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {

  @GetMapping("/api/v1/users/email/{email}")
  UserResponseDto getUserByEmail(@PathVariable("email") String email);

  @GetMapping("/api/v1/users/{id}")
  UserResponseDto getUserById(@PathVariable("id") Long id);
}
