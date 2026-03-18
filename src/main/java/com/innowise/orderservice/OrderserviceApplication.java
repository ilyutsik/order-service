package com.innowise.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableJpaAuditing
@EnableFeignClients
@SpringBootApplication
public class OrderserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderserviceApplication.class, args);
  }
}