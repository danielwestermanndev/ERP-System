// CustomerController.java
package com.dwestermann.erp.customer.controller;

import com.dwestermann.erp.customer.domain.Customer;
import com.dwestermann.erp.customer.domain.ContactPerson;
import com.dwestermann.erp.customer.domain.CustomerStatus;
import com.dwestermann.erp.customer.domain.CustomerType;
import com.dwestermann.erp.customer.dto.request.*;
import com.dwestermann.erp.customer.dto.response.*;
import com.dwestermann.erp.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customers and contact persons")
@PreAuthorize("hasPermission('customer', 'read')")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @Operation(summary = "Create a new customer", description = "Creates a new customer with optional primary contact person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Customer with email or number already exists")
    })
    @PostMapping
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {

        log.debug("Creating customer: {}", request.getName());

        Customer customer = customerMapper.toEntity(request);
        Customer createdCustomer = customerService.createCustomer(customer);
        CustomerResponse response = customerMapper.toResponse(createdCustomer);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves a customer by their unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {

        log.debug("Getting customer with ID: {}", id);

        Optional<Customer> customer = customerService.findById(id);
        return customer.map(c -> ResponseEntity.ok(customerMapper.toResponse(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update customer", description = "Updates an existing customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Email or customer number conflict")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {

        log.debug("Updating customer with ID: {}", id);

        // Ensure ID consistency
        request.setId(id);

        Customer customer = customerMapper.toEntity(request);
        Customer updatedCustomer = customerService.updateCustomer(customer);
        CustomerResponse response = customerMapper.toResponse(updatedCustomer);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete customer", description = "Deletes a customer (only if no dependencies exist)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Customer cannot be deleted due to dependencies")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('customer', 'delete')")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {

        log.debug("Deleting customer with ID: {}", id);

        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List all customers", description = "Retrieves a paginated list of customers")
    @GetMapping
    public ResponseEntity<CustomerListResponse> getAllCustomers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Show only active customers") @RequestParam(defaultValue = "false") boolean activeOnly) {

        log.debug("Getting customers list - page: {}, size: {}, activeOnly: {}", page, size, activeOnly);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Customer> customers = activeOnly ?
                customerService.findActiveCustomers(pageable) :
                customerService.findAllCustomers(pageable);

        CustomerListResponse response = customerMapper.toListResponse(customers);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search customers", description = "Search customers by various criteria")
    @PostMapping("/search")
    public ResponseEntity<CustomerListResponse> searchCustomers(
            @Valid @RequestBody CustomerSearchRequest searchRequest,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {

        log.debug("Searching customers with criteria: {}", searchRequest);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Customer> customers;

        if (searchRequest.getSearchTerm() != null && !searchRequest.getSearchTerm().trim().isEmpty()) {
            // General search
            customers = searchRequest.getActiveOnly() ?
                    customerService.searchActiveCustomers(searchRequest.getSearchTerm(), pageable) :
                    customerService.searchCustomers(searchRequest.getSearchTerm(), pageable);
        } else {
            // Advanced criteria search
            customers = customerService.findCustomersByCriteria(
                    searchRequest.getName(),
                    searchRequest.getEmail(),
                    searchRequest.getCity(),
                    searchRequest.getStatus(),
                    searchRequest.getType(),
                    pageable);
        }

        CustomerListResponse response = customerMapper.toListResponse(customers);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get customers by status", description = "Retrieves customers filtered by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<CustomerListResponse> getCustomersByStatus(
            @Parameter(description = "Customer status") @PathVariable CustomerStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting customers by status: {}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Customer> customers = customerService.findCustomersByStatus(status, pageable);
        CustomerListResponse response = customerMapper.toListResponse(customers);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get customers by type", description = "Retrieves customers filtered by type")
    @GetMapping("/type/{type}")
    public ResponseEntity<CustomerListResponse> getCustomersByType(
            @Parameter(description = "Customer type") @PathVariable CustomerType type,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting customers by type: {}", type);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<Customer> customers = customerService.findCustomersByType(type, pageable);
        CustomerListResponse response = customerMapper.toListResponse(customers);

        return ResponseEntity.ok(response);
    }

    // Status Management Endpoints

    @Operation(summary = "Activate customer", description = "Sets customer status to ACTIVE")
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<CustomerResponse> activateCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {

        log.debug("Activating customer with ID: {}", id);

        Customer customer = customerService.activateCustomer(id);
        CustomerResponse response = customerMapper.toResponse(customer);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate customer", description = "Sets customer status to INACTIVE")
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<CustomerResponse> deactivateCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {

        log.debug("Deactivating customer with ID: {}", id);

        Customer customer = customerService.deactivateCustomer(id);
        CustomerResponse response = customerMapper.toResponse(customer);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Archive customer", description = "Sets customer status to ARCHIVED")
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<CustomerResponse> archiveCustomer(
            @Parameter(description = "Customer ID") @PathVariable UUID id) {

        log.debug("Archiving customer with ID: {}", id);

        Customer customer = customerService.archiveCustomer(id);
        CustomerResponse response = customerMapper.toResponse(customer);

        return ResponseEntity.ok(response);
    }

    // Contact Person Management Endpoints

    @Operation(summary = "Get customer contacts", description = "Retrieves all contact persons for a customer")
    @GetMapping("/{customerId}/contacts")
    public ResponseEntity<List<ContactPersonResponse>> getCustomerContacts(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId) {

        log.debug("Getting contacts for customer with ID: {}", customerId);

        List<ContactPerson> contacts = customerService.findContactPersons(customerId);
        List<ContactPersonResponse> response = customerMapper.toContactPersonResponseList(contacts);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add contact person", description = "Adds a new contact person to a customer")
    @PostMapping("/{customerId}/contacts")
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<ContactPersonResponse> addContactPerson(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId,
            @Valid @RequestBody CreateContactPersonRequest request) {

        log.debug("Adding contact person to customer with ID: {}", customerId);

        ContactPerson contactPerson = customerMapper.toEntity(request);
        ContactPerson createdContact = customerService.addContactPerson(customerId, contactPerson);
        ContactPersonResponse response = customerMapper.toContactPersonResponse(createdContact);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update contact person", description = "Updates an existing contact person")
    @PutMapping("/{customerId}/contacts/{contactId}")
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<ContactPersonResponse> updateContactPerson(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId,
            @Parameter(description = "Contact Person ID") @PathVariable UUID contactId,
            @Valid @RequestBody UpdateContactPersonRequest request) {

        log.debug("Updating contact person with ID: {} for customer: {}", contactId, customerId);

        // Ensure ID consistency
        request.setId(contactId);

        ContactPerson contactPerson = customerMapper.toEntity(request);
        ContactPerson updatedContact = customerService.updateContactPerson(customerId, contactPerson);
        ContactPersonResponse response = customerMapper.toContactPersonResponse(updatedContact);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove contact person", description = "Removes a contact person from a customer")
    @DeleteMapping("/{customerId}/contacts/{contactId}")
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<Void> removeContactPerson(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId,
            @Parameter(description = "Contact Person ID") @PathVariable UUID contactId) {

        log.debug("Removing contact person with ID: {} from customer: {}", contactId, customerId);

        customerService.removeContactPerson(customerId, contactId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Set primary contact", description = "Sets a contact person as the primary contact")
    @PutMapping("/{customerId}/contacts/{contactId}/primary")
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<ContactPersonResponse> setPrimaryContact(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId,
            @Parameter(description = "Contact Person ID") @PathVariable UUID contactId) {

        log.debug("Setting primary contact with ID: {} for customer: {}", contactId, customerId);

        ContactPerson contactPerson = customerService.setPrimaryContact(customerId, contactId);
        ContactPersonResponse response = customerMapper.toContactPersonResponse(contactPerson);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get primary contact", description = "Retrieves the primary contact person for a customer")
    @GetMapping("/{customerId}/contacts/primary")
    public ResponseEntity<ContactPersonResponse> getPrimaryContact(
            @Parameter(description = "Customer ID") @PathVariable UUID customerId) {

        log.debug("Getting primary contact for customer with ID: {}", customerId);

        Optional<ContactPerson> primaryContact = customerService.findPrimaryContact(customerId);
        return primaryContact.map(contact -> ResponseEntity.ok(customerMapper.toContactPersonResponse(contact)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Validation and Utility Endpoints

    @Operation(summary = "Check email availability", description = "Checks if an email address is available for use")
    @GetMapping("/validate/email")
    public ResponseEntity<Boolean> checkEmailAvailability(
            @Parameter(description = "Email to check") @RequestParam String email,
            @Parameter(description = "Customer ID to exclude from check") @RequestParam(required = false) UUID excludeId) {

        log.debug("Checking email availability: {}", email);

        boolean available = customerService.isEmailAvailable(email, excludeId);
        return ResponseEntity.ok(available);
    }

    @Operation(summary = "Check customer number availability", description = "Checks if a customer number is available for use")
    @GetMapping("/validate/customer-number")
    public ResponseEntity<Boolean> checkCustomerNumberAvailability(
            @Parameter(description = "Customer number to check") @RequestParam String customerNumber,
            @Parameter(description = "Customer ID to exclude from check") @RequestParam(required = false) UUID excludeId) {

        log.debug("Checking customer number availability: {}", customerNumber);

        boolean available = customerService.isCustomerNumberAvailable(customerNumber, excludeId);
        return ResponseEntity.ok(available);
    }

    @Operation(summary = "Generate customer number", description = "Generates a new unique customer number")
    @GetMapping("/generate-number")
    @PreAuthorize("hasPermission('customer', 'write')")
    public ResponseEntity<String> generateCustomerNumber() {

        log.debug("Generating new customer number");

        String customerNumber = customerService.generateCustomerNumber();
        return ResponseEntity.ok(customerNumber);
    }

    @Operation(summary = "Get customer statistics", description = "Retrieves customer statistics for dashboard")
    @GetMapping("/statistics")
    @PreAuthorize("hasPermission('customer', 'read')")
    public ResponseEntity<CustomerStatsResponse> getCustomerStatistics() {

        log.debug("Getting customer statistics");

        CustomerStatsResponse stats = new CustomerStatsResponse();
        stats.setActiveCustomers(customerService.getActiveCustomerCount());
        stats.setInactiveCustomers(customerService.getCustomerCountByStatus(CustomerStatus.INACTIVE));
        stats.setArchivedCustomers(customerService.getCustomerCountByStatus(CustomerStatus.ARCHIVED));
        stats.setTotalCustomers(stats.getActiveCustomers() + stats.getInactiveCustomers() + stats.getArchivedCustomers());

        return ResponseEntity.ok(stats);
    }
}