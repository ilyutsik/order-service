package com.innowise.orderservice.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1294452282035304890L;

  public OrderNotFoundException(String message) {
    super(message);
  }

  public OrderNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public OrderNotFoundException(String field, String value) {
    super("Order not found by " + field + " : " + value);
  }
}
