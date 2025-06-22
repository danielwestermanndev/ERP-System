package com.dwestermann.erp.product.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateStockRequest {

    @NotNull(message = "New stock quantity is required")
    @Positive(message = "Stock quantity must be positive")
    private Integer newStockQuantity;

    private String reason;
    private String notes;
}