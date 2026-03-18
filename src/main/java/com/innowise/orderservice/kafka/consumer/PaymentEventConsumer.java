package com.innowise.orderservice.kafka.consumer;

import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.exception.OrderStatusTransitionException;
import com.innowise.orderservice.kafka.event.CreatePaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventConsumer {

  private final PaymentEventProcessor paymentEventProcessor;
  private final KafkaTemplate<String, CreatePaymentEvent> kafkaTemplate;

  @Value("${spring.kafka.topics.payment-events-dlq}")
  private String dlqTopic;

  @KafkaListener(
      topics = "${spring.kafka.topics.payment-events}",
      groupId = "${spring.kafka.consumer.group-id}"
  )
  public void onPaymentEvent(CreatePaymentEvent event) {
    try {
      paymentEventProcessor.processEvent(event);
      log.info("Payment event processed successfully for orderId={}", event.getOrderId());
    } catch (OrderNotFoundException ex) {
      log.warn("Order not found: orderId={}, event={}", event.getOrderId(), event, ex);
      kafkaTemplate.send(dlqTopic, event.getOrderId().toString(), event);
    } catch (OrderStatusTransitionException ex) {
      log.warn("Status transition rejected: orderId={}, event={}", event.getOrderId(), event, ex);
      kafkaTemplate.send(dlqTopic, event.getOrderId().toString(), event);
    } catch (Exception ex) {
      log.error("Unexpected error while processing payment event: orderId={}, event={}",
          event.getOrderId(), event, ex);
      throw ex;
    }
  }
}