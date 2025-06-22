package com.dwestermann.erp.customer.service.impl;

import com.dwestermann.erp.customer.domain.Customer;
import com.dwestermann.erp.customer.domain.ContactPerson;
import com.dwestermann.erp.customer.domain.CustomerStatus;
import com.dwestermann.erp.customer.domain.CustomerType;
import com.dwestermann.erp.customer.exception.CustomerNotFoundException;
import com.dwestermann.erp.customer.exception.DuplicateCustomerEmailException;
import com.dwestermann.erp.customer.exception.DuplicateCustomerNumberException;
import com.dwestermann.erp.customer.exception.InvalidCustomerOperationException;
import com.dwestermann.erp.customer.repository.CustomerRepository;
import com.dwestermann.erp.customer.repository.ContactPersonRepository;
import com.dwestermann.erp.customer.service.CustomerService;
import com.dwestermann.erp.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ContactPersonRepository contactPersonRepository;

    @Override
    public Customer createCustomer(Customer customer) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Creating customer for tenant: {}", tenantId);

        // Set tenant ID
        customer.setTenantId(tenantId);

        // Generate customer number if not provided
        if (customer.getCustomerNumber() == null || customer.getCustomerNumber().trim().isEmpty()) {
            customer.setCustomerNumber(generateCustomerNumber());
        }

        // Validate email uniqueness
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            Optional<Customer> existingCustomer = customerRepository.findByTenantIdAndEmail(tenantId, customer.getEmail());
            if (existingCustomer.isPresent()) {
                throw DuplicateCustomerEmailException.forEmail(customer.getEmail());
            }
        }

        // Validate customer number uniqueness
        if (customer.getCustomerNumber() != null) {
            Optional<Customer> existingCustomer = customerRepository.findByTenantIdAndCustomerNumber(tenantId, customer.getCustomerNumber());
            if (existingCustomer.isPresent()) {
                throw DuplicateCustomerNumberException.forNumber(customer.getCustomerNumber());
            }
        }

        // Set default status if not provided
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.ACTIVE);
        }

        // Set default type if not provided
        if (customer.getType() == null) {
            customer.setType(CustomerType.B2B);
        }

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Created customer with ID: {} for tenant: {}", savedCustomer.getId(), tenantId);

        return savedCustomer;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(UUID id) {
        String tenantId = TenantContext.getTenantId();
        return customerRepository.findByTenantIdAndId(tenantId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByCustomerNumber(String customerNumber) {
        String tenantId = TenantContext.getTenantId();
        return customerRepository.findByTenantIdAndCustomerNumber(tenantId, customerNumber);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Updating customer with ID: {} for tenant: {}", customer.getId(), tenantId);

        // Verify customer exists and belongs to tenant
        Customer existingCustomer = customerRepository.findByTenantIdAndId(tenantId, customer.getId())
                .orElseThrow(() -> CustomerNotFoundException.forId(customer.getId()));

        // Validate email uniqueness (excluding current customer)
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (customerRepository.existsByTenantIdAndEmailAndIdNot(tenantId, customer.getEmail(), customer.getId())) {
                throw DuplicateCustomerEmailException.forEmail(customer.getEmail());
            }
        }

        // Validate customer number uniqueness (excluding current customer)
        if (customer.getCustomerNumber() != null && !customer.getCustomerNumber().trim().isEmpty()) {
            if (customerRepository.existsByTenantIdAndCustomerNumberAndIdNot(tenantId, customer.getCustomerNumber(), customer.getId())) {
                throw DuplicateCustomerNumberException.forNumber(customer.getCustomerNumber());
            }
        }

        // Update fields on existing customer (preserves JPA version and audit fields)
        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhone(customer.getPhone());
        existingCustomer.setCustomerNumber(customer.getCustomerNumber());
        existingCustomer.setStatus(customer.getStatus());
        existingCustomer.setType(customer.getType());
        existingCustomer.setPrimaryAddress(customer.getPrimaryAddress());
        existingCustomer.setNotes(customer.getNotes());

        // Tenant ID stays the same from existing customer
        // Version will be automatically incremented by JPA

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Updated customer with ID: {} for tenant: {}", updatedCustomer.getId(), tenantId);

        return updatedCustomer;
    }

    @Override
    public void deleteCustomer(UUID id) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Deleting customer with ID: {} for tenant: {}", id, tenantId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> CustomerNotFoundException.forId(id));

        // Validate customer can be deleted (business rules)
        validateCustomerForDeletion(id);

        customerRepository.delete(customer);
        log.info("Deleted customer with ID: {} for tenant: {}", id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findAllCustomers(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return customerRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findActiveCustomers(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return customerRepository.findActiveCustomersByTenantId(tenantId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findCustomersByStatus(CustomerStatus status, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return customerRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findCustomersByType(CustomerType type, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return customerRepository.findByTenantIdAndType(tenantId, type, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> searchCustomers(String searchTerm, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllCustomers(pageable);
        }
        return customerRepository.findByTenantIdAndSearch(tenantId, searchTerm.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> searchActiveCustomers(String searchTerm, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findActiveCustomers(pageable);
        }
        return customerRepository.findActiveCustomersByTenantIdAndSearch(tenantId, searchTerm.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Customer> findCustomersByCriteria(String name, String email, String city,
                                                  CustomerStatus status, CustomerType type,
                                                  Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return customerRepository.findByTenantIdAndCriteria(tenantId, name, email, city, status, type, pageable);
    }

    @Override
    public Customer activateCustomer(UUID id) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Activating customer with ID: {} for tenant: {}", id, tenantId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> CustomerNotFoundException.forId(id));

        customer.activate();
        Customer activatedCustomer = customerRepository.save(customer);

        log.info("Activated customer with ID: {} for tenant: {}", id, tenantId);
        return activatedCustomer;
    }

    @Override
    public Customer deactivateCustomer(UUID id) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Deactivating customer with ID: {} for tenant: {}", id, tenantId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> CustomerNotFoundException.forId(id));

        customer.deactivate();
        Customer deactivatedCustomer = customerRepository.save(customer);

        log.info("Deactivated customer with ID: {} for tenant: {}", id, tenantId);
        return deactivatedCustomer;
    }

    @Override
    public Customer archiveCustomer(UUID id) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Archiving customer with ID: {} for tenant: {}", id, tenantId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, id)
                .orElseThrow(() -> CustomerNotFoundException.forId(id));

        // Business rule: Can only archive inactive customers
        if (customer.isActive()) {
            throw InvalidCustomerOperationException.cannotArchiveActiveCustomer(customer.getName());
        }

        customer.archive();
        Customer archivedCustomer = customerRepository.save(customer);

        log.info("Archived customer with ID: {} for tenant: {}", id, tenantId);
        return archivedCustomer;
    }

    @Override
    public ContactPerson addContactPerson(UUID customerId, ContactPerson contactPerson) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Adding contact person to customer with ID: {} for tenant: {}", customerId, tenantId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> CustomerNotFoundException.forId(customerId));

        // Set tenant ID for audit purposes
        contactPerson.setTenantId(tenantId);

        // Add contact person to customer (domain logic handles primary contact rules)
        customer.addContactPerson(contactPerson);

        customerRepository.save(customer);

        log.info("Added contact person to customer with ID: {} for tenant: {}", customerId, tenantId);
        return contactPerson;
    }

    @Override
    public ContactPerson updateContactPerson(UUID customerId, ContactPerson contactPerson) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Updating contact person for customer with ID: {} for tenant: {}", customerId, tenantId);

        // Verify customer exists and belongs to tenant
        Customer customer = customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> CustomerNotFoundException.forId(customerId));

        // Verify contact person exists and belongs to the customer
        ContactPerson existingContact = contactPersonRepository.findByIdAndTenantId(contactPerson.getId(), tenantId)
                .orElseThrow(() -> new RuntimeException("Contact person not found"));

        if (!existingContact.getCustomer().getId().equals(customerId)) {
            throw new InvalidCustomerOperationException("Contact person does not belong to the specified customer");
        }

        // Ensure tenant ID is not changed
        contactPerson.setTenantId(tenantId);
        contactPerson.setCustomer(customer);

        ContactPerson updatedContact = contactPersonRepository.save(contactPerson);
        log.info("Updated contact person for customer with ID: {} for tenant: {}", customerId, tenantId);

        return updatedContact;
    }

    @Override
    public void removeContactPerson(UUID customerId, UUID contactPersonId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Removing contact person from customer with ID: {} for tenant: {}", customerId, tenantId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> CustomerNotFoundException.forId(customerId));

        ContactPerson contactPerson = contactPersonRepository.findByIdAndTenantId(contactPersonId, tenantId)
                .orElseThrow(() -> CustomerNotFoundException.withMessage("Contact person not found"));

        // Remove contact person from customer (domain logic handles primary contact rules)
        customer.removeContactPerson(contactPerson);

        customerRepository.save(customer);

        log.info("Removed contact person from customer with ID: {} for tenant: {}", customerId, tenantId);
    }

    @Override
    public ContactPerson setPrimaryContact(UUID customerId, UUID contactPersonId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Setting primary contact for customer with ID: {} for tenant: {}", customerId, tenantId);

        Customer customer = customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> CustomerNotFoundException.forId(customerId));

        ContactPerson contactPerson = contactPersonRepository.findByIdAndTenantId(contactPersonId, tenantId)
                .orElseThrow(() -> CustomerNotFoundException.withMessage("Contact person not found"));

        // Set primary contact (domain logic handles validation)
        customer.setPrimaryContact(contactPerson);

        customerRepository.save(customer);

        log.info("Set primary contact for customer with ID: {} for tenant: {}", customerId, tenantId);
        return contactPerson;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactPerson> findPrimaryContact(UUID customerId) {
        String tenantId = TenantContext.getTenantId();
        return contactPersonRepository.findPrimaryContactByCustomerIdAndTenantId(customerId, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactPerson> findContactPersons(UUID customerId) {
        String tenantId = TenantContext.getTenantId();
        return contactPersonRepository.findByCustomerIdAndTenantId(customerId, tenantId);
    }

    @Override
    public String generateCustomerNumber() {
        String tenantId = TenantContext.getTenantId();

        // Generate customer number format: CUST-YYYY-NNNN
        String currentYear = String.valueOf(Year.now().getValue());
        String prefix = "CUST-" + currentYear + "-";

        // Find the highest existing customer number for current year
        long customerCount = customerRepository.countActiveCustomersByTenantId(tenantId) + 1;

        // Format with leading zeros (4 digits)
        String number = String.format("%04d", customerCount);

        String customerNumber = prefix + number;

        // Ensure uniqueness (in case of race conditions)
        while (customerRepository.findByTenantIdAndCustomerNumber(tenantId, customerNumber).isPresent()) {
            customerCount++;
            number = String.format("%04d", customerCount);
            customerNumber = prefix + number;
        }

        log.debug("Generated customer number: {} for tenant: {}", customerNumber, tenantId);
        return customerNumber;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email, UUID excludeCustomerId) {
        String tenantId = TenantContext.getTenantId();

        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        if (excludeCustomerId != null) {
            return !customerRepository.existsByTenantIdAndEmailAndIdNot(tenantId, email, excludeCustomerId);
        } else {
            return customerRepository.findByTenantIdAndEmail(tenantId, email).isEmpty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCustomerNumberAvailable(String customerNumber, UUID excludeCustomerId) {
        String tenantId = TenantContext.getTenantId();

        if (customerNumber == null || customerNumber.trim().isEmpty()) {
            return true;
        }

        if (excludeCustomerId != null) {
            return !customerRepository.existsByTenantIdAndCustomerNumberAndIdNot(tenantId, customerNumber, excludeCustomerId);
        } else {
            return customerRepository.findByTenantIdAndCustomerNumber(tenantId, customerNumber).isEmpty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveCustomerCount() {
        String tenantId = TenantContext.getTenantId();
        return customerRepository.countActiveCustomersByTenantId(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCustomerCountByStatus(CustomerStatus status) {
        String tenantId = TenantContext.getTenantId();
        return customerRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCustomerForDeletion(UUID customerId) {
        // TODO: Add business rule validations
        // - Check if customer has invoices
        // - Check if customer has open orders
        // - Check other referential integrity constraints

        // For now, we allow deletion
        // Later, this will be extended when Invoice and Order modules are implemented
        log.debug("Validating customer for deletion: {}", customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateContactPersonOperations(UUID customerId) {
        String tenantId = TenantContext.getTenantId();

        // Verify customer exists and belongs to tenant
        Customer customer = customerRepository.findByTenantIdAndId(tenantId, customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Additional validation logic can be added here
        log.debug("Validating contact person operations for customer: {}", customerId);
    }
}