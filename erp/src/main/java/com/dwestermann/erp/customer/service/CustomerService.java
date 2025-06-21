package com.dwestermann.erp.customer.service;

import com.dwestermann.erp.customer.domain.Customer;
import com.dwestermann.erp.customer.domain.ContactPerson;
import com.dwestermann.erp.customer.domain.CustomerStatus;
import com.dwestermann.erp.customer.domain.CustomerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {

    // Basic CRUD operations
    Customer createCustomer(Customer customer);
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByCustomerNumber(String customerNumber);
    Customer updateCustomer(Customer customer);
    void deleteCustomer(UUID id);

    // List and search operations
    Page<Customer> findAllCustomers(Pageable pageable);
    Page<Customer> findActiveCustomers(Pageable pageable);
    Page<Customer> findCustomersByStatus(CustomerStatus status, Pageable pageable);
    Page<Customer> findCustomersByType(CustomerType type, Pageable pageable);
    Page<Customer> searchCustomers(String searchTerm, Pageable pageable);
    Page<Customer> searchActiveCustomers(String searchTerm, Pageable pageable);

    // Advanced search
    Page<Customer> findCustomersByCriteria(String name, String email, String city,
                                           CustomerStatus status, CustomerType type,
                                           Pageable pageable);

    // Status management
    Customer activateCustomer(UUID id);
    Customer deactivateCustomer(UUID id);
    Customer archiveCustomer(UUID id);

    // Contact person management
    ContactPerson addContactPerson(UUID customerId, ContactPerson contactPerson);
    ContactPerson updateContactPerson(UUID customerId, ContactPerson contactPerson);
    void removeContactPerson(UUID customerId, UUID contactPersonId);
    ContactPerson setPrimaryContact(UUID customerId, UUID contactPersonId);
    Optional<ContactPerson> findPrimaryContact(UUID customerId);
    List<ContactPerson> findContactPersons(UUID customerId);

    // Business logic
    String generateCustomerNumber();
    boolean isEmailAvailable(String email, UUID excludeCustomerId);
    boolean isCustomerNumberAvailable(String customerNumber, UUID excludeCustomerId);

    // Statistics
    long getActiveCustomerCount();
    long getCustomerCountByStatus(CustomerStatus status);

    // Validation
    void validateCustomerForDeletion(UUID customerId);
    void validateContactPersonOperations(UUID customerId);
}