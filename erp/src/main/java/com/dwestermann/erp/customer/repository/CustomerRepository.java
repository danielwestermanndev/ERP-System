package com.dwestermann.erp.customer.repository;

import com.dwestermann.erp.customer.domain.Customer;
import com.dwestermann.erp.customer.domain.CustomerStatus;
import com.dwestermann.erp.customer.domain.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    // Basic tenant-aware queries

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId")
    Page<Customer> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.id = :id")
    Optional<Customer> findByTenantIdAndId(@Param("tenantId") String tenantId, @Param("id") UUID id);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.customerNumber = :customerNumber")
    Optional<Customer> findByTenantIdAndCustomerNumber(@Param("tenantId") String tenantId,
                                                       @Param("customerNumber") String customerNumber);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.email = :email")
    Optional<Customer> findByTenantIdAndEmail(@Param("tenantId") String tenantId, @Param("email") String email);

    // Status-based queries

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.status = :status")
    Page<Customer> findByTenantIdAndStatus(@Param("tenantId") String tenantId,
                                           @Param("status") CustomerStatus status,
                                           Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.status IN :statuses")
    Page<Customer> findByTenantIdAndStatusIn(@Param("tenantId") String tenantId,
                                             @Param("statuses") List<CustomerStatus> statuses,
                                             Pageable pageable);

    // Active customers only (most common use case)
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.status = 'ACTIVE'")
    Page<Customer> findActiveCustomersByTenantId(@Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.status = 'ACTIVE'")
    List<Customer> findAllActiveCustomersByTenantId(@Param("tenantId") String tenantId);

    // Type-based queries

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.type = :type")
    Page<Customer> findByTenantIdAndType(@Param("tenantId") String tenantId,
                                         @Param("type") CustomerType type,
                                         Pageable pageable);

    // Search queries

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> findByTenantIdAndSearch(@Param("tenantId") String tenantId,
                                           @Param("search") String search,
                                           Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId " +
            "AND c.status = 'ACTIVE' " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.customerNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> findActiveCustomersByTenantIdAndSearch(@Param("tenantId") String tenantId,
                                                          @Param("search") String search,
                                                          Pageable pageable);

    // Address-based search
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId " +
            "AND (LOWER(c.primaryAddress.city) LIKE LOWER(CONCAT('%', :city, '%')) " +
            "OR LOWER(c.primaryAddress.postalCode) LIKE LOWER(CONCAT('%', :city, '%')))")
    Page<Customer> findByTenantIdAndAddressCity(@Param("tenantId") String tenantId,
                                                @Param("city") String city,
                                                Pageable pageable);

    // Advanced search with multiple criteria
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId " +
            "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:city IS NULL OR LOWER(c.primaryAddress.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "AND (:type IS NULL OR c.type = :type)")
    Page<Customer> findByTenantIdAndCriteria(@Param("tenantId") String tenantId,
                                             @Param("name") String name,
                                             @Param("email") String email,
                                             @Param("city") String city,
                                             @Param("status") CustomerStatus status,
                                             @Param("type") CustomerType type,
                                             Pageable pageable);

    // Statistics queries

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tenantId = :tenantId AND c.status = 'ACTIVE'")
    long countActiveCustomersByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tenantId = :tenantId AND c.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") CustomerStatus status);

    @Query("SELECT c.type, COUNT(c) FROM Customer c WHERE c.tenantId = :tenantId AND c.status = 'ACTIVE' GROUP BY c.type")
    List<Object[]> getCustomerTypeStatistics(@Param("tenantId") String tenantId);

    // Business validation queries

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
            "WHERE c.tenantId = :tenantId AND c.email = :email AND c.id <> :excludeId")
    boolean existsByTenantIdAndEmailAndIdNot(@Param("tenantId") String tenantId,
                                             @Param("email") String email,
                                             @Param("excludeId") UUID excludeId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Customer c " +
            "WHERE c.tenantId = :tenantId AND c.customerNumber = :customerNumber AND c.id <> :excludeId")
    boolean existsByTenantIdAndCustomerNumberAndIdNot(@Param("tenantId") String tenantId,
                                                      @Param("customerNumber") String customerNumber,
                                                      @Param("excludeId") UUID excludeId);

    // Recent customers (useful for dashboard)
    @Query("SELECT c FROM Customer c WHERE c.tenantId = :tenantId AND c.status = 'ACTIVE' " +
            "ORDER BY c.createdAt DESC")
    List<Customer> findRecentActiveCustomers(@Param("tenantId") String tenantId, Pageable pageable);
}