package com.innowise.orderservice;

import com.innowise.orderservice.config.TestContainersConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public class IntegrationTestBase {

  @DynamicPropertySource
  static void configure(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", TestContainersConfig.POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", TestContainersConfig.POSTGRES::getUsername);
    registry.add("spring.datasource.password", TestContainersConfig.POSTGRES::getPassword);
  }
}