package com.dwestermann.erp.product.repository;

import com.dwestermann.erp.product.domain.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository // ✅ Explicit Repository annotation
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID> {

    // ==================== BASIC QUERIES ====================

    Optional<ProductCategory> findByIdAndTenantId(UUID id, String tenantId);

    List<ProductCategory> findByTenantIdOrderByNameAsc(String tenantId);

    Page<ProductCategory> findAllByOrderByNameAsc(Pageable pageable);

    // ==================== HIERARCHY QUERIES ====================

    @Query("SELECT c FROM ProductCategory c WHERE c.tenantId = :tenantId AND c.parentCategory IS NULL ORDER BY c.name ASC")
    List<ProductCategory> findRootCategoriesByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM ProductCategory c LEFT JOIN FETCH c.subcategories WHERE c.tenantId = :tenantId AND c.parentCategory IS NULL ORDER BY c.name ASC")
    List<ProductCategory> findRootCategoriesWithSubcategoriesByTenantId(@Param("tenantId") String tenantId);

    // Fallback method ohne tenantId für CategoryService Kompatibilität
    @Query("SELECT c FROM ProductCategory c LEFT JOIN FETCH c.subcategories WHERE c.parentCategory IS NULL ORDER BY c.name ASC")
    List<ProductCategory> findRootCategoriesWithSubcategories();

    List<ProductCategory> findByParentCategoryOrderByNameAsc(ProductCategory parentCategory);

    List<ProductCategory> findByParentCategoryAndTenantIdOrderByNameAsc(ProductCategory parentCategory, String tenantId);

    // ==================== SEARCH QUERIES ====================

    Page<ProductCategory> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String nameSearch, String descriptionSearch, Pageable pageable);

    @Query("SELECT c FROM ProductCategory c WHERE c.tenantId = :tenantId AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<ProductCategory> searchByTermAndTenantId(@Param("searchTerm") String searchTerm,
                                                  @Param("tenantId") String tenantId,
                                                  Pageable pageable);

    // ==================== COUNT QUERIES ====================

    Long countByParentCategoryId(UUID parentCategoryId);

    Long countByParentCategory(ProductCategory parentCategory);

    Long countByTenantId(String tenantId);

    @Query("SELECT COUNT(c) FROM ProductCategory c WHERE c.tenantId = :tenantId AND c.parentCategory IS NULL")
    Long countRootCategoriesByTenantId(@Param("tenantId") String tenantId);

    // Fallback für Service Kompatibilität
    @Query("SELECT COUNT(c) FROM ProductCategory c WHERE c.parentCategory IS NULL")
    Long countRootCategories();

    // ==================== VALIDATION QUERIES ====================

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN false ELSE true END FROM ProductCategory c WHERE " +
            "LOWER(c.name) = LOWER(:categoryName) AND c.tenantId = :tenantId AND " +
            "(:parentCategoryId IS NULL AND c.parentCategory IS NULL OR c.parentCategory.id = :parentCategoryId) AND " +
            "(:excludeCategoryId IS NULL OR c.id != :excludeCategoryId)")
    boolean isCategoryNameAvailableInTenant(@Param("categoryName") String categoryName,
                                            @Param("parentCategoryId") UUID parentCategoryId,
                                            @Param("excludeCategoryId") UUID excludeCategoryId,
                                            @Param("tenantId") String tenantId);

    // Fallback für Service Kompatibilität
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN false ELSE true END FROM ProductCategory c WHERE " +
            "LOWER(c.name) = LOWER(:categoryName) AND " +
            "(:parentCategoryId IS NULL AND c.parentCategory IS NULL OR c.parentCategory.id = :parentCategoryId) AND " +
            "(:excludeCategoryId IS NULL OR c.id != :excludeCategoryId)")
    boolean isCategoryNameAvailable(@Param("categoryName") String categoryName,
                                    @Param("parentCategoryId") UUID parentCategoryId,
                                    @Param("excludeCategoryId") UUID excludeCategoryId);

    // ==================== STATISTICS QUERIES ====================

    @Query("SELECT MAX(c.hierarchyDepth) FROM (SELECT SIZE(c.parentCategory) as hierarchyDepth FROM ProductCategory c) c")
    Integer findMaxHierarchyDepth();

    @Query("SELECT COUNT(c) FROM ProductCategory c WHERE c.productCount > 0")
    Long countCategoriesWithProducts();

    @Query("SELECT c FROM ProductCategory c WHERE c.productCount > 0 ORDER BY c.productCount DESC")
    List<ProductCategory> findTopCategoriesByProductCount(Pageable pageable);

    @Query("SELECT c FROM ProductCategory c WHERE c.productCount > 0 ORDER BY c.productCount DESC")
    List<ProductCategory> findTopCategoriesByValue(Pageable pageable);

    // ==================== BULK OPERATIONS ====================

    @Modifying
    @Transactional
    @Query("UPDATE ProductCategory c SET c.productCount = " +
            "(SELECT COUNT(p) FROM Product p WHERE p.category.id = c.id) " +
            "WHERE c.tenantId = :tenantId")
    void refreshProductCountsByTenantId(@Param("tenantId") String tenantId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductCategory c SET c.productCount = " +
            "(SELECT COUNT(p) FROM Product p WHERE p.category.id = c.id)")
    void refreshAllProductCounts();

    // ==================== EXISTENCE CHECKS ====================

    boolean existsByIdAndTenantId(UUID id, String tenantId);

    boolean existsByNameAndParentCategoryAndTenantId(String name, ProductCategory parentCategory, String tenantId);

    // ==================== TENANT-AWARE METHODS ====================

    @Query("SELECT c FROM ProductCategory c WHERE c.tenantId = :tenantId")
    List<ProductCategory> findAllByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM ProductCategory c WHERE c.tenantId = :tenantId")
    Page<ProductCategory> findAllByTenantId(@Param("tenantId") String tenantId, Pageable pageable);
}