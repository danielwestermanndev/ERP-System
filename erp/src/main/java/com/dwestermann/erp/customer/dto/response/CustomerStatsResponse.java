package com.dwestermann.erp.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatsResponse {

    private long totalCustomers;
    private long activeCustomers;
    private long inactiveCustomers;
    private long archivedCustomers;
    private Map<String, Long> customersByType; // B2B, B2C counts
    private long customersWithCompleteAddress;
    private long customersWithContacts;
}