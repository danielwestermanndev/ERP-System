package com.dwestermann.erp.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddStockRequest {

    @NotNull(message = "Quantity to add is required")
    @Positive(message = "Quantity to add must be positive")
    private Integer quantityToAdd;

    private String reason;
    private String notes;
    private String supplierReference;
}