package com.dwestermann.erp.product.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProductStockResponse {
    private UUID productId; // Geändert von Long zu UUID
    private String productName;
    private String sku;
    private Integer currentStock;
    private Integer minimumStockLevel;
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private Integer previousStock;
    private String operation;
    private Integer changeAmount;
    private String reason;
    private LocalDateTime lastStockUpdate;
    private String updatedBy;
}