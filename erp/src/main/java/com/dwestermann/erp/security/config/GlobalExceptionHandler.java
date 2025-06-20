package com.dwestermann.erp.config;

import com.dwestermann.erp.security.dto.MessageResponse;
import com.dwestermann.erp.security.exception.AuthenticationException;
import com.dwestermann.erp.security.exception.InvalidPasswordException;
import com.dwestermann.erp.security.exception.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<MessageResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        MessageResponse response = MessageResponse.builder()
                .message(ex.getMessage())
                .success(false)
                .timestamp(LocalDateTime.now())
                .code("AUTH_FAILED")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        log.warn("User registration failed: {}", ex.getMessage());

        MessageResponse response = MessageResponse.builder()
                .message(ex.getMessage())
                .success(false)
                .timestamp(LocalDateTime.now())
                .code("USER_EXISTS")
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<MessageResponse> handleInvalidPasswordException(InvalidPasswordException ex) {
        log.warn("Password validation failed: {}", ex.getMessage());

        MessageResponse response = MessageResponse.builder()
                .message(ex.getMessage())
                .success(false)
                .timestamp(LocalDateTime.now())
                .code("INVALID_PASSWORD")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        response.put("message", "Validation failed");
        response.put("success", false);
        response.put("timestamp", LocalDateTime.now());
        response.put("code", "VALIDATION_ERROR");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);

        MessageResponse response = MessageResponse.builder()
                .message("An unexpected error occurred")
                .success(false)
                .timestamp(LocalDateTime.now())
                .code("INTERNAL_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}