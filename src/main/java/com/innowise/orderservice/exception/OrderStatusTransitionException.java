package com.innowise.orderservice.exception;

import com.innowise.orderservice.model.entity.OrderStatus;
import java.io.Serial;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderStatusTransitionException extends RuntimeException{

  @Serial
  private static final long serialVersionUID = 4559222861474217464L;

  public OrderStatusTransitionException(OrderStatus from, OrderStatus to) {
    super(String.format("Cannot transition order status from %s to %s", from.name(), to.name()));
  }

  public OrderStatusTransitionException(String message) {
    super(message);
  }

  public OrderStatusTransitionException(String message, Throwable cause) {
    super(message, cause);
  }

  public OrderStatusTransitionException(Throwable cause) {
    super(cause);
  }
}
