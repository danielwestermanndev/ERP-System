package com.dwestermann.erp.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String status;
    private Object data;

    // ✅ Success field für Legacy-Kompatibilität
    private Boolean success;

    // ✅ Static factory methods für alle Use Cases
    public static MessageResponse success(String message) {
        return new MessageResponse(message, LocalDateTime.now(), "SUCCESS", null, true);
    }

    public static MessageResponse success(String message, Object data) {
        return new MessageResponse(message, LocalDateTime.now(), "SUCCESS", data, true);
    }

    public static MessageResponse error(String message) {
        return new MessageResponse(message, LocalDateTime.now(), "ERROR", null, false);
    }

    public static MessageResponse error(String message, Object data) {
        return new MessageResponse(message, LocalDateTime.now(), "ERROR", data, false);
    }

    public static MessageResponse warning(String message) {
        return new MessageResponse(message, LocalDateTime.now(), "WARNING", null, null);
    }

    public static MessageResponse warning(String message, Object data) {
        return new MessageResponse(message, LocalDateTime.now(), "WARNING", data, null);
    }

    public static MessageResponse info(String message) {
        return new MessageResponse(message, LocalDateTime.now(), "INFO", null, null);
    }

    public static MessageResponse info(String message, Object data) {
        return new MessageResponse(message, LocalDateTime.now(), "INFO", data, null);
    }

    // ✅ Builder methods für komplexere Fälle
    public static MessageResponseBuilder custom() {
        return MessageResponse.builder();
    }

    // ✅ Convenience methods
    public boolean isSuccess() {
        return success != null && success;
    }

    public boolean isError() {
        return "ERROR".equals(status) || (success != null && !success);
    }

    public boolean isWarning() {
        return "WARNING".equals(status);
    }

    public boolean isInfo() {
        return "INFO".equals(status);
    }
}