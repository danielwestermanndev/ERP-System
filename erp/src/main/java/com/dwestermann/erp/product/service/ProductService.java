package com.dwestermann.erp.product.service;

import com.dwestermann.erp.product.domain.ProductStatus;
import com.dwestermann.erp.product.dto.request.*;
import com.dwestermann.erp.product.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    // CRUD Operations
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse getProductById(UUID id); // Geändert von Long zu UUID
    ProductResponse updateProduct(UUID id, UpdateProductRequest request); // Geändert von Long zu UUID
    void deleteProduct(UUID id); // Geändert von Long zu UUID

    // Listing and Search
    ProductListResponse getAllProducts(Pageable pageable);
    ProductListResponse searchProducts(ProductSearchRequest request);
    ProductStatisticsResponse getProductStatistics();

    // Stock Management
    ProductStockResponse updateStock(UUID id, UpdateStockRequest request); // Geändert von Long zu UUID
    ProductStockResponse addStock(UUID id, AddStockRequest request); // Geändert von Long zu UUID
    ProductStockResponse removeStock(UUID id, RemoveStockRequest request); // Geändert von Long zu UUID

    // Validation
    ProductValidationResponse validateProduct(CreateProductRequest request);
    boolean isSkuAvailable(String sku, UUID excludeProductId); // Geändert von Long zu UUID

    // Bulk Operations
    BulkOperationResponse bulkUpdateStatus(List<UUID> productIds, ProductStatus newStatus); // Geändert von Long zu UUID
}