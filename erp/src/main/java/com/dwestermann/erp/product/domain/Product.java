package com.dwestermann.erp.product.domain;

import com.dwestermann.erp.common.entity.BaseEntity;
import com.dwestermann.erp.common.valueobject.Money;
import com.dwestermann.erp.product.exception.InsufficientStockException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_sku_tenant", columnList = "sku, tenantId", unique = true),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_status", columnList = "status"),
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_barcode", columnList = "barcode")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    // Core Identification
    @Column(nullable = false, length = 50)
    @NotBlank(message = "SKU ist erforderlich")
    @Size(max = 50, message = "SKU darf maximal 50 Zeichen lang sein")
    private String sku;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Produktname ist erforderlich")
    @Size(max = 255, message = "Produktname darf maximal 255 Zeichen lang sein")
    private String name;

    @Column(length = 1000)
    @Size(max = 1000, message = "Beschreibung darf maximal 1000 Zeichen lang sein")
    private String description;

    // Category Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    // Unit of Measurement
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Einheit ist erforderlich")
    private Unit unit = Unit.PIECE;

    // Pricing Information (Embedded Value Objects)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "purchase_price_amount", precision = 19, scale = 4)),
            @AttributeOverride(name = "currency", column = @Column(name = "purchase_price_currency", length = 3))
    })
    private Money purchasePrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "selling_price_amount", precision = 19, scale = 4)),
            @AttributeOverride(name = "currency", column = @Column(name = "selling_price_currency", length = 3))
    })
    private Money sellingPrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "rrp_amount", precision = 19, scale = 4)),
            @AttributeOverride(name = "currency", column = @Column(name = "rrp_currency", length = 3))
    })
    private Money recommendedRetailPrice;

    // Inventory Management
    @Column(precision = 19, scale = 4, nullable = false)
    @DecimalMin(value = "0.0", message = "Lagerbestand kann nicht negativ sein")
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    @DecimalMin(value = "0.0", message = "Mindestbestand kann nicht negativ sein")
    private BigDecimal minStockLevel = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    @DecimalMin(value = "0.0", message = "Maximalbestand kann nicht negativ sein")
    private BigDecimal maxStockLevel;

    // Business State
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status ist erforderlich")
    private ProductStatus status = ProductStatus.DRAFT;

    // Supplier Information
    @Column(length = 500)
    @Size(max = 500, message = "Lieferanteninformation darf maximal 500 Zeichen lang sein")
    private String supplierInfo;

    // Additional Metadata
    @Column(length = 100)
    @Size(max = 100, message = "Barcode darf maximal 100 Zeichen lang sein")
    private String barcode;

    @Column(precision = 10, scale = 4)
    @DecimalMin(value = "0.0", message = "Gewicht kann nicht negativ sein")
    private BigDecimal weight;

    @Column(length = 255)
    @Size(max = 255, message = "Bildpfad darf maximal 255 Zeichen lang sein")
    private String imagePath;

    @Column(nullable = false)
    private Boolean active = true;

    // -----------------------------------------------------------------------------
    // CONSTRUCTORS
    // -----------------------------------------------------------------------------

    public Product(String sku, String name, Unit unit, String tenantId) {
        this.sku = Objects.requireNonNull(sku, "SKU cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.unit = Objects.requireNonNull(unit, "Unit cannot be null");
        this.setTenantId(tenantId);
        this.status = ProductStatus.DRAFT;
        this.active = true;
    }

    // -----------------------------------------------------------------------------
    // BUSINESS METHODS
    // -----------------------------------------------------------------------------

    public void updatePricing(Money purchasePrice, Money sellingPrice, Money rrp) {
        this.purchasePrice = purchasePrice;
        this.sellingPrice = Objects.requireNonNull(sellingPrice, "Selling price is required");
        this.recommendedRetailPrice = rrp;

        validatePricing();
    }

    public void updateStock(BigDecimal newStock) {
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.currentStock = newStock;
    }

    public void addStock(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.currentStock = this.currentStock.add(quantity);
    }

    public void removeStock(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (this.currentStock.compareTo(quantity) < 0) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %s. Available: %s, Requested: %s",
                            sku, currentStock, quantity));
        }
        this.currentStock = this.currentStock.subtract(quantity);
    }

    public boolean isLowStock() {
        return minStockLevel != null &&
                currentStock.compareTo(minStockLevel) <= 0;
    }

    public boolean isOverStock() {
        return maxStockLevel != null &&
                currentStock.compareTo(maxStockLevel) > 0;
    }

    public void activate() {
        if (sellingPrice == null) {
            throw new IllegalStateException("Cannot activate product without selling price");
        }
        this.status = ProductStatus.ACTIVE;
        this.active = true;
    }

    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
        this.active = false;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return status == ProductStatus.ACTIVE && active != null && active;
    }

    public boolean canBeSold() {
        return isActive() && currentStock.compareTo(BigDecimal.ZERO) > 0;
    }

    public Money calculateMargin() {
        if (purchasePrice == null || sellingPrice == null) {
            return null;
        }
        return sellingPrice.subtract(purchasePrice);
    }

    public BigDecimal calculateMarginPercentage() {
        if (purchasePrice == null || sellingPrice == null ||
                purchasePrice.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal margin = calculateMargin().getAmount();
        return margin.divide(purchasePrice.getAmount(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    public String getDisplayName() {
        return String.format("%s - %s", sku, name);
    }

    public String getStockDisplayText() {
        if (unit != null && unit.allowsDecimals()) { // ✅ Null-Check hinzugefügt
            return String.format("%.2f %s", currentStock, unit.getSymbol());
        } else {
            String symbol = unit != null ? unit.getSymbol() : "";
            return String.format("%d %s", currentStock.intValue(), symbol);
        }
    }

    private void validatePricing() {
        if (purchasePrice != null && sellingPrice != null) {
            if (sellingPrice.getAmount().compareTo(purchasePrice.getAmount()) < 0) {
                throw new IllegalArgumentException("Selling price cannot be lower than purchase price");
            }
        }

        if (recommendedRetailPrice != null && sellingPrice != null) {
            if (recommendedRetailPrice.getAmount().compareTo(sellingPrice.getAmount()) < 0) {
                throw new IllegalArgumentException("Recommended retail price cannot be lower than selling price");
            }
        }
    }

    // -----------------------------------------------------------------------------
    // JPA LIFECYCLE CALLBACKS
    // -----------------------------------------------------------------------------

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();

        // Validiere required fields
        if (this.currentStock == null) {
            this.currentStock = BigDecimal.ZERO;
        }
        if (this.minStockLevel == null) {
            this.minStockLevel = BigDecimal.ZERO;
        }
        if (this.active == null) {
            this.active = true;
        }
    }

    @PreUpdate
    @Override
    protected void onUpdate() {
        super.onUpdate();

        // Zusätzliche Validierung bei Updates
        if (this.active == null) {
            this.active = true;
        }
    }

    // -----------------------------------------------------------------------------
    // EQUALS & HASHCODE
    // -----------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;

        Product product = (Product) o;

        // Primary: Compare by ID if both exist
        if (this.getId() != null && product.getId() != null) {
            return this.getId().equals(product.getId());
        }

        // Secondary: Compare by SKU + tenantId for business equality
        return Objects.equals(sku, product.sku) &&
                Objects.equals(getTenantId(), product.getTenantId());
    }

    @Override
    public int hashCode() {
        // If ID exists, use it
        if (getId() != null) {
            return getId().hashCode();
        }

        // If no ID, use sku + tenantId
        return Objects.hash(sku, getTenantId());
    }

    @Override
    public String toString() {
        return String.format("Product{id='%s', sku='%s', name='%s', status=%s}",
                getId(), sku, name, status);
    }
}