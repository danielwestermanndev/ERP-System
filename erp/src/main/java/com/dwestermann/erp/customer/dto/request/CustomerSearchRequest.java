package com.dwestermann.erp.customer.dto.request;

import com.dwestermann.erp.customer.domain.CustomerStatus;
import com.dwestermann.erp.customer.domain.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSearchRequest {

    private String name;
    private String email;
    private String city;
    private CustomerStatus status;
    private CustomerType type;
    private String searchTerm; // General search across name, email, customer number
    private Boolean activeOnly = false;
}