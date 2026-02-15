package com.innowise.orderservice.exception;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(String field, String value) {
    super("User with " + field + " : " + value + " not found");
  }
}
