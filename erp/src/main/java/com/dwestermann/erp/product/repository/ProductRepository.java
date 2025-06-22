package com.dwestermann.erp.product.repository;

import com.dwestermann.erp.product.domain.Product;
import com.dwestermann.erp.product.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    // ==================== BASIC QUERIES ====================

    Optional<Product> findBySkuAndTenantId(String sku, String tenantId);

    boolean existsBySkuAndTenantId(String sku, String tenantId);

    Page<Product> findByTenantIdOrderByNameAsc(String tenantId, Pageable pageable);

    List<Product> findByTenantIdOrderByNameAsc(String tenantId);

    // ==================== CATEGORY-BASED QUERIES ====================

    List<Product> findByCategoryIdAndTenantId(UUID categoryId, String tenantId);

    Page<Product> findByCategoryIdAndTenantId(UUID categoryId, String tenantId, Pageable pageable);

    Long countByCategoryId(UUID categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.tenantId = :tenantId")
    Long countByCategoryIdAndTenantId(@Param("categoryId") UUID categoryId, @Param("tenantId") String tenantId);

    // ==================== SEARCH QUERIES ====================

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchByTerm(@Param("searchTerm") String searchTerm,
                               @Param("tenantId") String tenantId,
                               Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> findByNameContainingIgnoreCaseAndTenantId(@Param("searchTerm") String searchTerm,
                                                            @Param("tenantId") String tenantId);

    // ==================== STATUS-BASED QUERIES ====================

    List<Product> findByStatusAndTenantId(ProductStatus status, String tenantId);

    Page<Product> findByStatusAndTenantId(ProductStatus status, String tenantId, Pageable pageable);

    Long countByStatusAndTenantId(ProductStatus status, String tenantId);

    // Alternative method name for compatibility
    Long countByStatus(ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status AND p.tenantId = :tenantId")
    Long countByStatusAndTenant(@Param("status") ProductStatus status, @Param("tenantId") String tenantId);

    // ==================== STOCK-BASED QUERIES ====================

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
            "p.currentStock <= p.minStockLevel AND p.minStockLevel > 0")
    List<Product> findLowStockProducts(@Param("tenantId") String tenantId);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND p.currentStock = 0")
    List<Product> findOutOfStockProducts(@Param("tenantId") String tenantId);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
            "p.maxStockLevel IS NOT NULL AND p.currentStock > p.maxStockLevel")
    List<Product> findOverStockProducts(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND " +
            "p.currentStock <= p.minStockLevel AND p.minStockLevel > 0")
    Long countLowStockProductsByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.currentStock = 0")
    Long countOutOfStockProductsByCategoryId(@Param("categoryId") UUID categoryId);

    // ==================== VALUE CALCULATIONS ====================

    @Query("SELECT COALESCE(SUM(p.sellingPrice.amount * p.currentStock), 0) " +
            "FROM Product p WHERE p.category.id = :categoryId")
    BigDecimal calculateTotalValueByCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT COALESCE(SUM(p.sellingPrice.amount * p.currentStock), 0) " +
            "FROM Product p WHERE p.tenantId = :tenantId AND p.sellingPrice.amount IS NOT NULL")
    BigDecimal calculateTotalInventoryValue(@Param("tenantId") String tenantId);

    @Query("SELECT COALESCE(SUM(p.purchasePrice.amount * p.currentStock), 0) " +
            "FROM Product p WHERE p.tenantId = :tenantId AND p.purchasePrice.amount IS NOT NULL")
    BigDecimal calculateTotalPurchaseValue(@Param("tenantId") String tenantId);

    // ==================== SKU VALIDATION ====================

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN false ELSE true END FROM Product p WHERE " +
            "LOWER(p.sku) = LOWER(:sku) AND p.tenantId = :tenantId AND " +
            "(:excludeId IS NULL OR p.id != :excludeId)")
    boolean isSkuAvailable(@Param("sku") String sku,
                           @Param("tenantId") String tenantId,
                           @Param("excludeId") UUID excludeId);

    // Alternative method for simple SKU checking
    boolean existsBySkuIgnoreCaseAndTenantId(String sku, String tenantId);

    // ==================== STATISTICS QUERIES ====================

    @Query("SELECT COUNT(p) FROM Product p WHERE p.tenantId = :tenantId")
    Long countByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(DISTINCT p.category) FROM Product p WHERE p.tenantId = :tenantId AND p.category IS NOT NULL")
    Long countDistinctCategoriesByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.tenantId = :tenantId AND p.active = true")
    Long countActiveProductsByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT AVG(p.sellingPrice.amount) FROM Product p WHERE p.tenantId = :tenantId AND p.sellingPrice.amount IS NOT NULL")
    BigDecimal calculateAverageSellingPrice(@Param("tenantId") String tenantId);

    // ==================== BARCODE QUERIES ====================

    Optional<Product> findByBarcodeAndTenantId(String barcode, String tenantId);

    boolean existsByBarcodeAndTenantId(String barcode, String tenantId);

    // ==================== RECENT PRODUCTS ====================

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId ORDER BY p.createdAt DESC")
    List<Product> findRecentProducts(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId ORDER BY p.updatedAt DESC")
    List<Product> findRecentlyUpdatedProducts(@Param("tenantId") String tenantId, Pageable pageable);

    // ==================== ADVANCED FILTERING ====================

    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:minPrice IS NULL OR p.sellingPrice.amount >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.sellingPrice.amount <= :maxPrice)")
    Page<Product> findWithFilters(@Param("tenantId") String tenantId,
                                  @Param("categoryId") UUID categoryId,
                                  @Param("status") ProductStatus status,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  Pageable pageable);

    // ==================== BULK OPERATIONS ====================

    @Query("SELECT p FROM Product p WHERE p.id IN :productIds AND p.tenantId = :tenantId")
    List<Product> findByIdsAndTenantId(@Param("productIds") List<UUID> productIds, @Param("tenantId") String tenantId);

    // ==================== UTILITY METHODS ====================

    @Query("SELECT DISTINCT p.unit FROM Product p WHERE p.tenantId = :tenantId")
    List<String> findDistinctUnitsByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT p.sellingPrice.currency FROM Product p WHERE p.tenantId = :tenantId AND p.sellingPrice.currency IS NOT NULL GROUP BY p.sellingPrice.currency")
    List<String> findDistinctCurrenciesByTenantId(@Param("tenantId") String tenantId);
}