package com.dwestermann.erp.product.domain;

import com.dwestermann.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "product_categories", indexes = {
        @Index(name = "idx_category_name_parent_tenant", columnList = "name, parent_category_id, tenantId", unique = true),
        @Index(name = "idx_category_parent", columnList = "parent_category_id"),
        @Index(name = "idx_category_name", columnList = "name"),
        @Index(name = "idx_category_tenant", columnList = "tenantId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductCategory extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Kategoriename ist erforderlich")
    @Size(max = 100, message = "Kategoriename darf maximal 100 Zeichen lang sein")
    private String name;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Beschreibung darf maximal 500 Zeichen lang sein")
    private String description;

    // Self-referencing relationship for hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private ProductCategory parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductCategory> subcategories = new ArrayList<>();

    // Product count (denormalized for performance)
    @Column(name = "product_count")
    private Long productCount = 0L; // ✅ Null-safe default

    // Constructor
    public ProductCategory(String name, String description, ProductCategory parentCategory, String tenantId) {
        this.name = Objects.requireNonNull(name, "Category name cannot be null");
        this.description = description;
        this.parentCategory = parentCategory;
        this.setTenantId(tenantId);
        this.productCount = 0L;
    }

    // ==================== HIERARCHY METHODS ====================

    public void addSubcategory(ProductCategory subcategory) {
        if (subcategory != null) {
            subcategories.add(subcategory);
            subcategory.setParentCategory(this);
            subcategory.setTenantId(this.getTenantId());
        }
    }

    public void removeSubcategory(ProductCategory subcategory) {
        if (subcategory != null) {
            subcategories.remove(subcategory);
            subcategory.setParentCategory(null);
        }
    }

    public boolean isRootCategory() {
        return parentCategory == null;
    }

    public boolean hasSubcategories() {
        return subcategories != null && !subcategories.isEmpty();
    }

    public boolean hasProducts() {
        return productCount != null && productCount > 0; // ✅ Null-safe check
    }

    public int getHierarchyDepth() {
        int depth = 0;
        ProductCategory current = this.parentCategory;
        while (current != null) {
            depth++;
            current = current.getParentCategory();
        }
        return depth;
    }

    public List<ProductCategory> getRootPath() {
        List<ProductCategory> path = new ArrayList<>();
        ProductCategory current = this;
        while (current != null) {
            path.add(0, current);
            current = current.getParentCategory();
        }
        return path;
    }

    public String getFullPath() {
        List<ProductCategory> path = getRootPath();
        return path.stream()
                .map(ProductCategory::getName)
                .reduce((a, b) -> a + " > " + b)
                .orElse(name);
    }

    // ==================== BUSINESS METHODS ====================

    public void incrementProductCount() {
        if (this.productCount == null) {
            this.productCount = 0L;
        }
        this.productCount++;
    }

    public void decrementProductCount() {
        if (this.productCount == null) {
            this.productCount = 0L;
        } else if (this.productCount > 0) {
            this.productCount--;
        }
    }

    public void updateProductCount(long count) {
        this.productCount = Math.max(0L, count);
    }

    public String getDisplayName() {
        if (isRootCategory()) {
            return name;
        } else {
            return getFullPath();
        }
    }

    public String getShortDisplayName() {
        if (name.length() > 30) {
            return name.substring(0, 27) + "...";
        }
        return name;
    }

    // ==================== JPA LIFECYCLE CALLBACKS ====================

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();

        // Ensure productCount is never null
        if (this.productCount == null) {
            this.productCount = 0L;
        }

        // Normalize name
        if (this.name != null) {
            this.name = this.name.trim();
        }
    }

    @PreUpdate
    @Override
    protected void onUpdate() {
        super.onUpdate();

        // Ensure productCount is never null
        if (this.productCount == null) {
            this.productCount = 0L;
        }

        // Normalize name
        if (this.name != null) {
            this.name = this.name.trim();
        }
    }

    // ==================== VALIDATION ====================

    public boolean isValidHierarchy() {
        // Check for circular references
        ProductCategory current = this.parentCategory;
        while (current != null) {
            if (current.getId() != null && current.getId().equals(this.getId())) {
                return false; // Circular reference detected
            }
            current = current.getParentCategory();
        }
        return true;
    }

    public boolean canBeDeleted() {
        return !hasProducts() && !hasSubcategories();
    }

    // ==================== EQUALS & HASHCODE ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCategory)) return false;

        ProductCategory category = (ProductCategory) o;

        // Primary: Compare by ID if both exist
        if (this.getId() != null && category.getId() != null) {
            return this.getId().equals(category.getId());
        }

        // Secondary: Business equality - name + parent + tenant
        return Objects.equals(name, category.name) &&
                Objects.equals(getParentCategoryId(), category.getParentCategoryId()) &&
                Objects.equals(getTenantId(), category.getTenantId());
    }

    @Override
    public int hashCode() {
        // If ID exists, use it
        if (getId() != null) {
            return getId().hashCode();
        }

        // If no ID, use name + parent + tenant
        return Objects.hash(name, getParentCategoryId(), getTenantId());
    }

    // Helper method for equals/hashCode
    private Object getParentCategoryId() {
        return parentCategory != null ? parentCategory.getId() : null;
    }

    @Override
    public String toString() {
        return String.format("ProductCategory{id=%s, name='%s', parentId=%s, productCount=%d}",
                getId(), name, getParentCategoryId(), productCount != null ? productCount : 0L);
    }
}