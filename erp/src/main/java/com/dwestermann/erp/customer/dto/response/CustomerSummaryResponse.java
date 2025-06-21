package com.dwestermann.erp.customer.dto.response;

import com.dwestermann.erp.customer.domain.CustomerStatus;
import com.dwestermann.erp.customer.domain.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryResponse {

    private UUID id;
    private String name;
    private String email;
    private String customerNumber;
    private CustomerStatus status;
    private CustomerType type;
    private String city; // From primary address
    private String primaryContactName;
    private int contactCount;
    private String displayName;
    private LocalDateTime createdAt;
}