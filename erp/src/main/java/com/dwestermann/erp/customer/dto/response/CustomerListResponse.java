package com.dwestermann.erp.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListResponse {

    private List<CustomerSummaryResponse> customers;
    private PaginationResponse pagination;
}