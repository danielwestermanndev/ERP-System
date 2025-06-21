package com.dwestermann.erp.customer.dto.response;

import com.dwestermann.erp.customer.domain.CustomerStatus;
import com.dwestermann.erp.customer.domain.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String customerNumber;
    private CustomerStatus status;
    private CustomerType type;
    private AddressResponse primaryAddress;
    private String notes;
    private List<ContactPersonResponse> contacts;
    private ContactPersonResponse primaryContact;
    private int contactCount;
    private String displayName;
    private boolean hasCompleteAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}