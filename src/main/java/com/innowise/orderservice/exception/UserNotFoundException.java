package com.innowise.orderservice.exception;

import java.io.Serial;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UserNotFoundException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -4353819725771786541L;

  public UserNotFoundException(String message) {
    super(message);
  }

  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotFoundException(String field, String value) {
    super("User with " + field + " : " + value + " not found");
  }
}
