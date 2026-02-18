package com.innowise.orderservice.exception;

import com.innowise.orderservice.model.entity.OrderStatus;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Order Not Found",
        ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ItemNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleItemNotFound(ItemNotFoundException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Item Not Found",
        ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User Not Found",
        ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseValidation> handleValidationException(
      MethodArgumentNotValidException ex) {

    Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(
        Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage,
            (msg1, msg2) -> msg1 + "; " + msg2));

    ErrorResponseValidation response = new ErrorResponseValidation(HttpStatus.BAD_REQUEST.value(),
        "Validation Failed", errors);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException ex) {

    String error = null;
    if (ex.getName().equals("statuses")) {

      String invalidValue = (String) ex.getValue();

      error = String.format("Invalid status value: '%s'", invalidValue);
    }

    ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
        "Validation Failed", error);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal ServerError", ex.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
