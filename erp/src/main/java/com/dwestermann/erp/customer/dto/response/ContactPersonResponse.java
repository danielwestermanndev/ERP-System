package com.dwestermann.erp.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactPersonResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String position;
    private Boolean isPrimary;
    private boolean hasEmail;
    private boolean hasPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}