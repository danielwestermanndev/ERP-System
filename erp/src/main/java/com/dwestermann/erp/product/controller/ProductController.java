package com.dwestermann.erp.product.controller;

import com.dwestermann.erp.product.dto.request.*;
import com.dwestermann.erp.product.dto.response.*;
import com.dwestermann.erp.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    // ==================== CRUD OPERATIONS ====================

    @PostMapping
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Create a new product", description = "Creates a new product with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Product with SKU already exists")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.debug("Creating new product with SKU: {}", request.getSku());

        ProductResponse response = productService.createProduct(request);

        log.info("Created product: {} (ID: {})", response.getName(), response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get product by ID", description = "Retrieves a product by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable UUID id) {
        log.debug("Retrieving product with ID: {}", id);

        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Update product", description = "Updates an existing product with new details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.debug("Updating product ID: {} with new data", id);

        ProductResponse response = productService.updateProduct(id, request);

        log.info("Updated product: {} (ID: {})", response.getName(), response.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('product', 'delete')")
    @Operation(summary = "Delete product", description = "Deletes a product by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Product cannot be deleted due to dependencies")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable UUID id) {
        log.debug("Deleting product with ID: {}", id);

        productService.deleteProduct(id);

        log.info("Deleted product with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== LISTING AND SEARCH ====================

    @GetMapping
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get all products", description = "Retrieves a paginated list of all products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ProductListResponse> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDirection) {

        log.debug("Retrieving products - page: {}, size: {}, sortBy: {}, direction: {}",
                page, size, sortBy, sortDirection);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        ProductListResponse response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Search products", description = "Searches products with advanced filtering options")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ProductListResponse> searchProducts(
            @Valid @RequestBody ProductSearchRequest request) {
        log.debug("Searching products with criteria: {}", request.getSearchTerm());

        ProductListResponse response = productService.searchProducts(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get product statistics", description = "Retrieves comprehensive product statistics for dashboard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ProductStatisticsResponse> getProductStatistics() {
        log.debug("Retrieving product statistics");

        ProductStatisticsResponse response = productService.getProductStatistics();
        return ResponseEntity.ok(response);
    }

    // ==================== STOCK MANAGEMENT ====================

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Update product stock", description = "Updates the stock quantity of a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid stock data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ProductStockResponse> updateStock(
            @Parameter(description = "Product ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateStockRequest request) {
        log.debug("Updating stock for product ID: {} to quantity: {}", id, request.getNewStockQuantity());

        ProductStockResponse response = productService.updateStock(id, request);

        log.info("Updated stock for product ID: {} to quantity: {}", id, response.getCurrentStock());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stock/add")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Add stock", description = "Adds stock quantity to a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid stock data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ProductStockResponse> addStock(
            @Parameter(description = "Product ID") @PathVariable UUID id,
            @Valid @RequestBody AddStockRequest request) {
        log.debug("Adding {} stock to product ID: {}", request.getQuantityToAdd(), id);

        ProductStockResponse response = productService.addStock(id, request);

        log.info("Added {} stock to product ID: {}, new quantity: {}",
                request.getQuantityToAdd(), id, response.getCurrentStock());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stock/remove")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Remove stock", description = "Removes stock quantity from a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid stock data or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ProductStockResponse> removeStock(
            @Parameter(description = "Product ID") @PathVariable UUID id,
            @Valid @RequestBody RemoveStockRequest request) {
        log.debug("Removing {} stock from product ID: {}", request.getQuantityToRemove(), id);

        ProductStockResponse response = productService.removeStock(id, request);

        log.info("Removed {} stock from product ID: {}, new quantity: {}",
                request.getQuantityToRemove(), id, response.getCurrentStock());
        return ResponseEntity.ok(response);
    }

    // ==================== VALIDATION AND UTILITY ====================

    @PostMapping("/validate")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Validate product data", description = "Validates product data without creating the product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validation completed"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ProductValidationResponse> validateProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.debug("Validating product data for SKU: {}", request.getSku());

        ProductValidationResponse response = productService.validateProduct(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}/available")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Check SKU availability", description = "Checks if a SKU is available for use")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SKU availability checked"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Boolean> isSkuAvailable(
            @Parameter(description = "SKU to check") @PathVariable String sku,
            @Parameter(description = "Product ID to exclude from check") @RequestParam(required = false) UUID excludeProductId) {
        log.debug("Checking SKU availability: {}", sku);

        boolean available = productService.isSkuAvailable(sku, excludeProductId);
        return ResponseEntity.ok(available);
    }

    // ==================== BULK OPERATIONS ====================

    @PostMapping("/bulk/update-status")
    @PreAuthorize("hasPermission('product', 'write')")
    @Operation(summary = "Bulk update product status", description = "Updates status for multiple products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk operation completed"),
            @ApiResponse(responseCode = "400", description = "Invalid bulk operation data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<BulkOperationResponse> bulkUpdateStatus(
            @RequestBody BulkStatusUpdateRequest request) {
        log.debug("Bulk updating status for {} products", request.getProductIds().size());

        BulkOperationResponse response = productService.bulkUpdateStatus(request.getProductIds(), request.getNewStatus());

        log.info("Bulk status update completed: {} successful, {} failed",
                response.getSuccessfulOperations(), response.getFailedOperations());
        return ResponseEntity.ok(response);
    }

    // ==================== PRODUCT VARIANTS (Future Enhancement) ====================

    @GetMapping("/{id}/variants")
    @PreAuthorize("hasPermission('product', 'read')")
    @Operation(summary = "Get product variants", description = "Retrieves variants of a product (future feature)")
    public ResponseEntity<List<ProductSummaryResponse>> getProductVariants(
            @Parameter(description = "Product ID") @PathVariable UUID id) {
        log.debug("Retrieving variants for product ID: {}", id);
        // TODO: Implement product variants in future version
        return ResponseEntity.ok().build();
    }
}