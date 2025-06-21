package com.dwestermann.erp.customer.repository;

import com.dwestermann.erp.customer.domain.Customer;
import com.dwestermann.erp.customer.domain.CustomerStatus;
import com.dwestermann.erp.customer.domain.CustomerType;
import com.dwestermann.erp.customer.domain.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer testCustomer;
    private final String TENANT_ID = "test-tenant";

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("test@example.com");
        testCustomer.setCustomerNumber("CUST-2024-0001");
        testCustomer.setTenantId(TENANT_ID);
        testCustomer.setStatus(CustomerStatus.ACTIVE);
        testCustomer.setType(CustomerType.B2B);
        testCustomer.setPrimaryAddress(new Address("Test Street 1", "Test City", "12345", "Germany"));
    }

    @Test
    @DisplayName("Should save and find customer by tenant and ID")
    void shouldSaveAndFindCustomerByTenantAndId() {
        // Save customer
        Customer savedCustomer = customerRepository.save(testCustomer);
        entityManager.flush();

        // Find by tenant and ID
        Optional<Customer> found = customerRepository.findByTenantIdAndId(TENANT_ID, savedCustomer.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Customer", found.get().getName());
        assertEquals(TENANT_ID, found.get().getTenantId());
    }

    @Test
    @DisplayName("Should not find customer from different tenant")
    void shouldNotFindCustomerFromDifferentTenant() {
        // Save customer
        Customer savedCustomer = customerRepository.save(testCustomer);
        entityManager.flush();

        // Try to find with different tenant ID
        Optional<Customer> found = customerRepository.findByTenantIdAndId("different-tenant", savedCustomer.getId());

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find customer by email and tenant")
    void shouldFindCustomerByEmailAndTenant() {
        // Save customer
        customerRepository.save(testCustomer);
        entityManager.flush();

        // Find by email
        Optional<Customer> found = customerRepository.findByTenantIdAndEmail(TENANT_ID, "test@example.com");

        assertTrue(found.isPresent());
        assertEquals("Test Customer", found.get().getName());
    }

    @Test
    @DisplayName("Should find customers by status")
    void shouldFindCustomersByStatus() {
        // Save active customer
        customerRepository.save(testCustomer);

        // Create inactive customer
        Customer inactiveCustomer = new Customer();
        inactiveCustomer.setName("Inactive Customer");
        inactiveCustomer.setEmail("inactive@example.com");
        inactiveCustomer.setCustomerNumber("CUST-2024-0002");
        inactiveCustomer.setTenantId(TENANT_ID);
        inactiveCustomer.setStatus(CustomerStatus.INACTIVE);
        inactiveCustomer.setType(CustomerType.B2C);
        inactiveCustomer.setPrimaryAddress(new Address("Inactive Street", "Inactive City", "54321", "Germany"));
        customerRepository.save(inactiveCustomer);

        entityManager.flush();

        // Find active customers
        Page<Customer> activeCustomers = customerRepository.findByTenantIdAndStatus(
                TENANT_ID, CustomerStatus.ACTIVE, PageRequest.of(0, 10));

        assertEquals(1, activeCustomers.getTotalElements());
        assertEquals("Test Customer", activeCustomers.getContent().get(0).getName());

        // Find inactive customers
        Page<Customer> inactiveCustomers = customerRepository.findByTenantIdAndStatus(
                TENANT_ID, CustomerStatus.INACTIVE, PageRequest.of(0, 10));

        assertEquals(1, inactiveCustomers.getTotalElements());
        assertEquals("Inactive Customer", inactiveCustomers.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Should search customers by name")
    void shouldSearchCustomersByName() {
        customerRepository.save(testCustomer);
        entityManager.flush();

        Page<Customer> results = customerRepository.findByTenantIdAndSearch(
                TENANT_ID, "Test", PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("Test Customer", results.getContent().get(0).getName());
    }

    @Test
    @DisplayName("Should count active customers")
    void shouldCountActiveCustomers() {
        customerRepository.save(testCustomer);
        entityManager.flush();

        long count = customerRepository.countActiveCustomersByTenantId(TENANT_ID);

        assertEquals(1, count);
    }

    @Test
    @DisplayName("Should check email uniqueness excluding specific customer")
    void shouldCheckEmailUniquenessExcludingSpecificCustomer() {
        Customer savedCustomer = customerRepository.save(testCustomer);
        entityManager.flush();

        // Should return false when excluding the same customer
        boolean exists = customerRepository.existsByTenantIdAndEmailAndIdNot(
                TENANT_ID, "test@example.com", savedCustomer.getId());

        assertFalse(exists);

        // Should return true when not excluding the customer
        boolean existsWithoutExclusion = customerRepository.findByTenantIdAndEmail(
                TENANT_ID, "test@example.com").isPresent();

        assertTrue(existsWithoutExclusion);
    }
}