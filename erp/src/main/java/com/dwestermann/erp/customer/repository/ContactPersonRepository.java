package com.dwestermann.erp.customer.repository;

import com.dwestermann.erp.customer.domain.ContactPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactPersonRepository extends JpaRepository<ContactPerson, UUID> {

    // Find contact persons by customer (tenant-aware through customer relationship)
    @Query("SELECT cp FROM ContactPerson cp WHERE cp.customer.id = :customerId AND cp.customer.tenantId = :tenantId")
    List<ContactPerson> findByCustomerIdAndTenantId(@Param("customerId") UUID customerId,
                                                    @Param("tenantId") String tenantId);

    // Find specific contact person
    @Query("SELECT cp FROM ContactPerson cp WHERE cp.id = :id AND cp.customer.tenantId = :tenantId")
    Optional<ContactPerson> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") String tenantId);

    // Find primary contact for customer
    @Query("SELECT cp FROM ContactPerson cp WHERE cp.customer.id = :customerId " +
            "AND cp.customer.tenantId = :tenantId AND cp.isPrimary = true")
    Optional<ContactPerson> findPrimaryContactByCustomerIdAndTenantId(@Param("customerId") UUID customerId,
                                                                      @Param("tenantId") String tenantId);

    // Find contact persons by email (for duplicate check across customers)
    @Query("SELECT cp FROM ContactPerson cp WHERE cp.customer.tenantId = :tenantId AND cp.email = :email")
    List<ContactPerson> findByTenantIdAndEmail(@Param("tenantId") String tenantId, @Param("email") String email);

    // Search contact persons across all customers for a tenant
    @Query("SELECT cp FROM ContactPerson cp WHERE cp.customer.tenantId = :tenantId " +
            "AND (LOWER(cp.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(cp.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(cp.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ContactPerson> findByTenantIdAndSearch(@Param("tenantId") String tenantId, @Param("search") String search);

    // Business validation
    @Query("SELECT CASE WHEN COUNT(cp) > 0 THEN true ELSE false END FROM ContactPerson cp " +
            "WHERE cp.customer.id = :customerId AND cp.customer.tenantId = :tenantId AND cp.isPrimary = true")
    boolean hasPrimaryContactForCustomer(@Param("customerId") UUID customerId, @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(cp) FROM ContactPerson cp WHERE cp.customer.id = :customerId AND cp.customer.tenantId = :tenantId")
    long countByCustomerIdAndTenantId(@Param("customerId") UUID customerId, @Param("tenantId") String tenantId);
}