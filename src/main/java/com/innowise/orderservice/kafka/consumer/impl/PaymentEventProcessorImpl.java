package com.innowise.orderservice.kafka.consumer.impl;

import com.innowise.orderservice.kafka.consumer.PaymentEventProcessor;
import com.innowise.orderservice.kafka.event.CreatePaymentEvent;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentEventProcessorImpl implements PaymentEventProcessor {

  private static final String SUCCESS = "SUCCESS";

  private final OrderService service;

  @Transactional
  @Override
  public void processEvent(CreatePaymentEvent event) {
    OrderStatus newStatus = event.getPaymentStatus().equals(SUCCESS) ?
        OrderStatus.PAID : OrderStatus.CANCELLED;

    Long orderId = event.getOrderId();

    service.updateStatusById(orderId, newStatus);
  }
}