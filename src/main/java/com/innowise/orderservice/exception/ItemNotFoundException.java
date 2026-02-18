package com.innowise.orderservice.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ItemNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -6891364686295202749L;

  public ItemNotFoundException(String message) {
    super(message);
  }

  public ItemNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public ItemNotFoundException(String field, String value) {
    super("Item with " + field + " : " + value + " not found");
  }
}
