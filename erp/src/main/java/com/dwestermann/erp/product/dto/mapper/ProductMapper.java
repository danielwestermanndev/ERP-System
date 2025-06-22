package com.dwestermann.erp.product.dto.mapper;

import com.dwestermann.erp.common.valueobject.Money;
import com.dwestermann.erp.product.domain.Product;
import com.dwestermann.erp.product.domain.ProductCategory;
import com.dwestermann.erp.product.domain.ProductStatus;
import com.dwestermann.erp.product.dto.request.CreateProductRequest;
import com.dwestermann.erp.product.dto.request.UpdateProductRequest;
import com.dwestermann.erp.product.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    // ==================== ENTITY TO RESPONSE MAPPING ====================

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getSellingPrice() != null ? product.getSellingPrice().getAmount() : null)
                .currency(product.getSellingPrice() != null ? product.getSellingPrice().getCurrency() : "EUR")
                .formattedPrice(formatPrice(
                        product.getSellingPrice() != null ? product.getSellingPrice().getAmount() : null,
                        product.getSellingPrice() != null ? product.getSellingPrice().getCurrency() : "EUR"))
                .unit(product.getUnit())
                .unitDisplayName(product.getUnit() != null ? product.getUnit().getDisplayName() : "")
                .stockQuantity(product.getCurrentStock() != null ? product.getCurrentStock().intValue() : 0)
                .minimumStockLevel(product.getMinStockLevel() != null ? product.getMinStockLevel().intValue() : 0)
                .status(product.getStatus())
                .statusDisplayName(getStatusDisplayName(product.getStatus()))
                .category(product.getCategory() != null ? mapCategoryBasic(product.getCategory()) : null)
                .notes(product.getNotes())
                .isLowStock(isLowStock(product))
                .isOutOfStock(product.getCurrentStock() != null && product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .createdBy(product.getCreatedBy())
                .updatedBy(product.getUpdatedBy())
                .version(product.getVersion())
                .build();
    }

    public ProductSummaryResponse toSummaryResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getSellingPrice() != null ? product.getSellingPrice().getAmount() : null)
                .currency(product.getSellingPrice() != null ? product.getSellingPrice().getCurrency() : "EUR")
                .formattedPrice(formatPrice(
                        product.getSellingPrice() != null ? product.getSellingPrice().getAmount() : null,
                        product.getSellingPrice() != null ? product.getSellingPrice().getCurrency() : "EUR"))
                .unit(product.getUnit())
                .stockQuantity(product.getCurrentStock() != null ? product.getCurrentStock().intValue() : 0)
                .status(product.getStatus())
                .statusDisplayName(getStatusDisplayName(product.getStatus()))
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .isLowStock(isLowStock(product))
                .isOutOfStock(product.getCurrentStock() != null && product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0)
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public ProductStockResponse toStockResponse(Product product, String operation, Integer previousStock, String reason) {
        if (product == null) {
            return null;
        }

        Integer currentStockInt = product.getCurrentStock() != null ? product.getCurrentStock().intValue() : 0;
        Integer changeAmount = null;
        if (previousStock != null) {
            changeAmount = currentStockInt - previousStock;
        }

        return ProductStockResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .sku(product.getSku())
                .currentStock(currentStockInt)
                .minimumStockLevel(product.getMinStockLevel() != null ? product.getMinStockLevel().intValue() : 0)
                .isLowStock(isLowStock(product))
                .isOutOfStock(product.getCurrentStock() != null && product.getCurrentStock().compareTo(BigDecimal.ZERO) == 0)
                .previousStock(previousStock)
                .operation(operation)
                .changeAmount(changeAmount)
                .reason(reason)
                .lastStockUpdate(product.getUpdatedAt())
                .updatedBy(product.getUpdatedBy())
                .build();
    }

    // ==================== LIST MAPPING ====================

    public List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProductSummaryResponse> toSummaryResponseList(List<Product> products) {
        return products.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public ProductListResponse toListResponse(Page<Product> productPage) {
        return ProductListResponse.builder()
                .products(toSummaryResponseList(productPage.getContent()))
                .pagination(toPaginationResponse(productPage))
                .build();
    }

    // ==================== REQUEST TO ENTITY MAPPING ====================

    public Product toEntity(CreateProductRequest request, ProductCategory category) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());

        // Money/Price handling mit Money Value Object
        if (request.getPrice() != null) {
            String currency = request.getCurrency() != null ? request.getCurrency() : "EUR";
            product.setSellingPrice(new Money(request.getPrice(), currency));
        }

        product.setUnit(request.getUnit());

        // Convert Integer to BigDecimal for entity
        if (request.getStockQuantity() != null) {
            product.setCurrentStock(BigDecimal.valueOf(request.getStockQuantity()));
        }
        if (request.getMinimumStockLevel() != null) {
            product.setMinStockLevel(BigDecimal.valueOf(request.getMinimumStockLevel()));
        }

        product.setStatus(request.getStatus());
        product.setCategory(category);
        product.setNotes(request.getNotes());
        product.setBarcode(request.getBarcode());
        product.setWeight(request.getWeight());
        product.setSupplierInfo(request.getSupplierInfo());

        return product;
    }

    public void updateEntity(Product product, UpdateProductRequest request, ProductCategory category) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());

        // Update selling price mit Money Value Object
        if (request.getPrice() != null) {
            String currency = request.getCurrency() != null ? request.getCurrency() : "EUR";
            product.setSellingPrice(new Money(request.getPrice(), currency));
        }

        product.setUnit(request.getUnit());

        // Convert Integer to BigDecimal for entity
        if (request.getMinimumStockLevel() != null) {
            product.setMinStockLevel(BigDecimal.valueOf(request.getMinimumStockLevel()));
        }

        product.setStatus(request.getStatus());
        product.setCategory(category);
        product.setNotes(request.getNotes());
        product.setBarcode(request.getBarcode());
        product.setWeight(request.getWeight());
        product.setSupplierInfo(request.getSupplierInfo());
    }

    // ==================== HELPER METHODS ====================

    private PaginationResponse toPaginationResponse(Page<?> page) {
        return PaginationResponse.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    private String formatPrice(BigDecimal amount, String currencyCode) {
        if (amount == null) {
            return currencyCode + " 0,00";
        }
        try {
            Currency currency = Currency.getInstance(currencyCode);
            NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
            formatter.setCurrency(currency);
            return formatter.format(amount);
        } catch (Exception e) {
            return currencyCode + " " + String.format("%.2f", amount);
        }
    }

    private String getStatusDisplayName(ProductStatus status) {
        if (status == null) return "";
        return switch (status) {
            case DRAFT -> "Entwurf";
            case ACTIVE -> "Aktiv";
            case DISCONTINUED -> "Eingestellt";
            case OUT_OF_STOCK -> "Nicht verfügbar";
        };
    }

    private boolean isLowStock(Product product) {
        if (product.getCurrentStock() == null || product.getMinStockLevel() == null) {
            return false;
        }
        return product.getCurrentStock().compareTo(product.getMinStockLevel()) < 0;
    }

    private CategoryResponse mapCategoryBasic(ProductCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}