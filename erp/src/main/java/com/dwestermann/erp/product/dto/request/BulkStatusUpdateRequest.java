package com.dwestermann.erp.product.dto.request;

import com.dwestermann.erp.product.domain.ProductStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkStatusUpdateRequest {
    @NotEmpty(message = "Product IDs list cannot be empty")
    private List<UUID> productIds; // Geändert von Long zu UUID

    @NotNull(message = "New status is required")
    private ProductStatus newStatus;

    private String reason;
}