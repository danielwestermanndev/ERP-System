package com.dwestermann.erp.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductListResponse {
    private List<ProductSummaryResponse> products;
    private PaginationResponse pagination;  // ← Verschachtelt!
}