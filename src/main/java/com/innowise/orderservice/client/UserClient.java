package com.innowise.orderservice.client;

import com.innowise.orderservice.model.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {


  @GetMapping("/api/v1/users/{id}")
  UserResponseDto getUserById(@PathVariable("id") Long id,
      @RequestHeader("X-USER-ID") Long userId, @RequestHeader("X-USER-ROLE") String role);
}
