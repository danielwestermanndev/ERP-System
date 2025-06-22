package com.dwestermann.erp.product.service.impl;

import com.dwestermann.erp.product.domain.Product;
import com.dwestermann.erp.product.domain.ProductCategory;
import com.dwestermann.erp.product.domain.ProductStatus;
import com.dwestermann.erp.product.dto.mapper.ProductMapper;
import com.dwestermann.erp.product.dto.request.*;
import com.dwestermann.erp.product.dto.response.*;
import com.dwestermann.erp.product.exception.*;
import com.dwestermann.erp.product.repository.ProductRepository;
import com.dwestermann.erp.product.service.ProductCategoryService;
import com.dwestermann.erp.product.service.ProductService;
import com.dwestermann.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryService categoryService;
    private final ProductMapper productMapper;

    // ==================== CRUD OPERATIONS ====================

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        log.debug("Creating new product with SKU: {}", request.getSku());

        String tenantId = TenantContext.getTenantId();

        // Validate SKU uniqueness
        if (productRepository.existsBySkuAndTenantId(request.getSku(), tenantId)) {
            throw new DuplicateSkuException("Product with SKU '" + request.getSku() + "' already exists");
        }

        // Validate category if provided
        ProductCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryEntityById(request.getCategoryId());
        }

        // Create and save product
        Product product = productMapper.toEntity(request, category);
        product.setTenantId(tenantId);
        product = productRepository.save(product);

        log.info("Created product: {} (ID: {})", product.getName(), product.getId());
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = getProductEntityById(id);
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        log.debug("Updating product ID: {} with new data", id);

        Product product = getProductEntityById(id);

        // Validate category if provided
        ProductCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryEntityById(request.getCategoryId());
        }

        // Update product
        productMapper.updateEntity(product, request, category);
        product = productRepository.save(product);

        log.info("Updated product: {} (ID: {})", product.getName(), product.getId());
        return productMapper.toResponse(product);
    }

    @Override
    public void deleteProduct(UUID id) {
        log.debug("Deleting product with ID: {}", id);

        Product product = getProductEntityById(id);

        // Business validation - check if product can be deleted
        // TODO: Add validation for existing orders, invoices, etc.

        productRepository.delete(product);
        log.info("Deleted product: {} (ID: {})", product.getName(), product.getId());
    }

    // ==================== LISTING AND SEARCH ====================

    @Override
    @Transactional(readOnly = true)
    public ProductListResponse getAllProducts(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Page<Product> productPage = productRepository.findByTenantIdOrderByNameAsc(tenantId, pageable);
        return productMapper.toListResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResponse searchProducts(ProductSearchRequest request) {
        log.debug("Searching products with criteria: {}", request.getSearchTerm());

        String tenantId = TenantContext.getTenantId();

        // Simple text search implementation
        Page<Product> productPage;
        if (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) {
            productPage = productRepository.searchByTerm(
                    request.getSearchTerm().trim(),
                    tenantId,
                    request.toPageable()
            );
        } else {
            // If no search term, return all products with filters
            productPage = productRepository.findByTenantIdOrderByNameAsc(tenantId, request.toPageable());
        }

        return productMapper.toListResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductStatisticsResponse getProductStatistics() {
        log.debug("Calculating product statistics");

        String tenantId = TenantContext.getTenantId();

        Long totalProducts = productRepository.countByTenantId(tenantId);
        Long activeProducts = productRepository.countByStatusAndTenantId(ProductStatus.ACTIVE, tenantId);
        Long draftProducts = productRepository.countByStatusAndTenantId(ProductStatus.DRAFT, tenantId);
        Long discontinuedProducts = productRepository.countByStatusAndTenantId(ProductStatus.DISCONTINUED, tenantId);

        List<Product> lowStockProducts = productRepository.findLowStockProducts(tenantId);
        List<Product> outOfStockProducts = productRepository.findOutOfStockProducts(tenantId);

        BigDecimal totalInventoryValue = productRepository.calculateTotalInventoryValue(tenantId);
        Long categoriesWithProducts = productRepository.countDistinctCategoriesByTenantId(tenantId);

        return ProductStatisticsResponse.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .draftProducts(draftProducts)
                .discontinuedProducts(discontinuedProducts)
                .lowStockProducts((long) lowStockProducts.size())
                .outOfStockProducts((long) outOfStockProducts.size())
                .totalInventoryValue(totalInventoryValue)
                .totalInventoryValueFormatted(formatCurrency(totalInventoryValue, "EUR"))
                .categoriesWithProducts(categoriesWithProducts)
                .lowStockProductsList(productMapper.toSummaryResponseList(lowStockProducts.stream().limit(10).collect(Collectors.toList())))
                .outOfStockProductsList(productMapper.toSummaryResponseList(outOfStockProducts.stream().limit(10).collect(Collectors.toList())))
                .build();
    }

    // ==================== STOCK MANAGEMENT ====================

    @Override
    public ProductStockResponse updateStock(UUID id, UpdateStockRequest request) {
        log.debug("Updating stock for product ID: {} to quantity: {}", id, request.getNewStockQuantity());

        Product product = getProductEntityById(id);
        Integer previousStock = product.getCurrentStock() != null ? product.getCurrentStock().intValue() : 0;

        product.updateStock(BigDecimal.valueOf(request.getNewStockQuantity()));
        product = productRepository.save(product);

        log.info("Updated stock for product ID: {} from {} to {}", id, previousStock, request.getNewStockQuantity());

        return productMapper.toStockResponse(product, "UPDATE", previousStock, request.getReason());
    }

    @Override
    public ProductStockResponse addStock(UUID id, AddStockRequest request) {
        log.debug("Adding {} stock to product ID: {}", request.getQuantityToAdd(), id);

        Product product = getProductEntityById(id);
        Integer previousStock = product.getCurrentStock() != null ? product.getCurrentStock().intValue() : 0;

        product.addStock(BigDecimal.valueOf(request.getQuantityToAdd()));
        product = productRepository.save(product);

        Integer newStock = product.getCurrentStock().intValue();
        log.info("Added {} stock to product ID: {}, new total: {}", request.getQuantityToAdd(), id, newStock);

        return productMapper.toStockResponse(product, "ADD", previousStock, request.getReason());
    }

    @Override
    public ProductStockResponse removeStock(UUID id, RemoveStockRequest request) {
        log.debug("Removing {} stock from product ID: {}", request.getQuantityToRemove(), id);

        Product product = getProductEntityById(id);
        Integer previousStock = product.getCurrentStock() != null ? product.getCurrentStock().intValue() : 0;

        product.removeStock(BigDecimal.valueOf(request.getQuantityToRemove()));
        product = productRepository.save(product);

        Integer newStock = product.getCurrentStock().intValue();
        log.info("Removed {} stock from product ID: {}, new total: {}", request.getQuantityToRemove(), id, newStock);

        return productMapper.toStockResponse(product, "REMOVE", previousStock, request.getReason());
    }

    // ==================== VALIDATION ====================

    @Override
    @Transactional(readOnly = true)
    public ProductValidationResponse validateProduct(CreateProductRequest request) {
        log.debug("Validating product data for SKU: {}", request.getSku());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        String tenantId = TenantContext.getTenantId();

        // SKU validation
        if (productRepository.existsBySkuAndTenantId(request.getSku(), tenantId)) {
            errors.add("SKU '" + request.getSku() + "' already exists");
        }

        // Category validation
        if (request.getCategoryId() != null) {
            try {
                categoryService.getCategoryEntityById(request.getCategoryId());
            } catch (CategoryNotFoundException e) {
                errors.add("Category not found");
            }
        }

        // Business validation
        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            warnings.add("Price should be greater than zero");
        }

        if (request.getStockQuantity() != null && request.getStockQuantity() < 0) {
            errors.add("Stock quantity cannot be negative");
        }

        return ProductValidationResponse.builder()
                .valid(errors.isEmpty())
                .errors(errors)
                .warnings(warnings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSkuAvailable(String sku, UUID excludeProductId) {
        String tenantId = TenantContext.getTenantId();
        return productRepository.isSkuAvailable(sku, tenantId, excludeProductId);
    }

    // ==================== BULK OPERATIONS ====================

    @Override
    public BulkOperationResponse bulkUpdateStatus(List<UUID> productIds, ProductStatus newStatus) {
        log.debug("Bulk updating status for {} products to {}", productIds.size(), newStatus);

        String tenantId = TenantContext.getTenantId();
        List<String> errors = new ArrayList<>();
        int successful = 0;

        for (UUID productId : productIds) {
            try {
                Product product = getProductEntityById(productId);
                product.setStatus(newStatus);

                // Apply business rules based on status
                if (newStatus == ProductStatus.ACTIVE) {
                    product.activate();
                } else if (newStatus == ProductStatus.DISCONTINUED) {
                    product.discontinue();
                }

                productRepository.save(product);
                successful++;
            } catch (Exception e) {
                errors.add("Failed to update product " + productId + ": " + e.getMessage());
            }
        }

        log.info("Bulk status update completed: {} successful, {} failed", successful, errors.size());

        return BulkOperationResponse.builder()
                .totalRequested(productIds.size())
                .successfulOperations(successful)
                .failedOperations(errors.size())
                .errors(errors)
                .operationType("BULK_STATUS_UPDATE")
                .build();
    }

    // ==================== HELPER METHODS ====================

    @Transactional(readOnly = true)
    public Product getProductEntityById(UUID id) {
        String tenantId = TenantContext.getTenantId();
        return productRepository.findById(id)
                .filter(product -> product.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
    }

    private String formatCurrency(BigDecimal amount, String currency) {
        if (amount == null) {
            return currency + " 0,00";
        }
        return currency + " " + amount.toString();
    }
}