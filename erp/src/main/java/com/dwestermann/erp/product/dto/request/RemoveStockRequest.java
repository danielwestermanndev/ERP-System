package com.dwestermann.erp.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RemoveStockRequest {

    @NotNull(message = "Quantity to remove is required")
    @Positive(message = "Quantity to remove must be positive")
    private Integer quantityToRemove;

    private String reason;
    private String notes;
    private String orderReference;
}