package com.innowise.orderservice.kafka.consumer;

import com.innowise.orderservice.kafka.event.CreatePaymentEvent;

public interface PaymentEventProcessor {

  void processEvent(CreatePaymentEvent event);
}