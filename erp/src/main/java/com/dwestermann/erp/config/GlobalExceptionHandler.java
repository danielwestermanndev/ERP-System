package com.dwestermann.erp.config;

import com.dwestermann.erp.customer.exception.CustomerNotFoundException;
import com.dwestermann.erp.customer.exception.DuplicateCustomerEmailException;
import com.dwestermann.erp.customer.exception.DuplicateCustomerNumberException;
import com.dwestermann.erp.customer.exception.InvalidCustomerOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(
            CustomerNotFoundException ex, WebRequest request) {

        log.error("Customer not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "CUSTOMER_NOT_FOUND",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateCustomerEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCustomerEmailException(
            DuplicateCustomerEmailException ex, WebRequest request) {

        log.error("Duplicate customer email: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "DUPLICATE_CUSTOMER_EMAIL",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DuplicateCustomerNumberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCustomerNumberException(
            DuplicateCustomerNumberException ex, WebRequest request) {

        log.error("Duplicate customer number: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "DUPLICATE_CUSTOMER_NUMBER",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidCustomerOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCustomerOperationException(
            InvalidCustomerOperationException ex, WebRequest request) {

        log.error("Invalid customer operation: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_CUSTOMER_OPERATION",
                ex.getMessage(),
                LocalDateTime.now(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed for one or more fields",
                fieldErrors,
                LocalDateTime.now(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Generic exception handler for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error occurred: ", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                LocalDateTime.now(),
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // Error response DTOs
    public static class ErrorResponse {
        private String code;
        private String message;
        private LocalDateTime timestamp;
        private String path;

        public ErrorResponse(String code, String message, LocalDateTime timestamp, String path) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
            this.path = path;
        }

        // Getters
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getPath() { return path; }
    }

    public static class ValidationErrorResponse extends ErrorResponse {
        private Map<String, String> fieldErrors;

        public ValidationErrorResponse(String code, String message, Map<String, String> fieldErrors,
                                       LocalDateTime timestamp, String path) {
            super(code, message, timestamp, path);
            this.fieldErrors = fieldErrors;
        }

        public Map<String, String> getFieldErrors() { return fieldErrors; }
    }
}