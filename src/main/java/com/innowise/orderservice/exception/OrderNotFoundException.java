package com.innowise.orderservice.exception;

public class OrderNotFoundException extends RuntimeException {

  public OrderNotFoundException(String field, String value) {
    super("Order not found by " + field + " : " + value);
  }
}
