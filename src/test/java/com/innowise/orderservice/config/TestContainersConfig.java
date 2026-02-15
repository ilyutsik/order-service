package com.innowise.orderservice.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersConfig {

  private TestContainersConfig() {
  }

  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
      "postgres:14").withDatabaseName("testDB").withUsername("123").withPassword("123");

  static {
    POSTGRES.start();
  }
}
