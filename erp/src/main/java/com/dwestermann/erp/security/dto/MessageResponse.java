package com.dwestermann.erp.security.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {

    private String message;
    private boolean success;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String code; // Optional error/success code
}