package com.dwestermann.erp.customer.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer customer;
    private Address address;
    private ContactPerson primaryContact;
    private ContactPerson secondaryContact;

    @BeforeEach
    void setUp() {
        address = new Address("Musterstraße 1", "Berlin", "10115", "Germany");

        customer = new Customer();
        customer.setName("ACME Corporation");
        customer.setEmail("contact@acme.com");
        customer.setPhone("+49 30 12345678");
        customer.setCustomerNumber("CUST-2024-0001");
        customer.setPrimaryAddress(address);
        customer.setType(CustomerType.B2B);
        customer.setStatus(CustomerStatus.ACTIVE);

        primaryContact = new ContactPerson();
        primaryContact.setFirstName("John");
        primaryContact.setLastName("Doe");
        primaryContact.setEmail("john.doe@acme.com");
        primaryContact.setPhone("+49 30 12345679");
        primaryContact.setPosition("CEO");
        primaryContact.setIsPrimary(true);

        secondaryContact = new ContactPerson();
        secondaryContact.setFirstName("Jane");
        secondaryContact.setLastName("Smith");
        secondaryContact.setEmail("jane.smith@acme.com");
        secondaryContact.setPosition("CFO");
        secondaryContact.setIsPrimary(false);
    }

    @Test
    @DisplayName("Should create customer with valid data")
    void shouldCreateCustomerWithValidData() {
        assertNotNull(customer);
        assertEquals("ACME Corporation", customer.getName());
        assertEquals("contact@acme.com", customer.getEmail());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertEquals(CustomerType.B2B, customer.getType());
        assertTrue(customer.isActive());
        assertFalse(customer.isArchived());
    }

    @Test
    @DisplayName("Should activate and deactivate customer")
    void shouldActivateAndDeactivateCustomer() {
        // Test activation
        customer.setStatus(CustomerStatus.INACTIVE);
        customer.activate();
        assertTrue(customer.isActive());
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());

        // Test deactivation
        customer.deactivate();
        assertFalse(customer.isActive());
        assertEquals(CustomerStatus.INACTIVE, customer.getStatus());

        // Test archiving
        customer.archive();
        assertTrue(customer.isArchived());
        assertEquals(CustomerStatus.ARCHIVED, customer.getStatus());
    }

    @Test
    @DisplayName("Should add contact person and set as primary if first")
    void shouldAddContactPersonAndSetAsPrimaryIfFirst() {
        // Add first contact - should become primary automatically
        customer.addContactPerson(primaryContact);

        assertEquals(1, customer.getContactCount());
        assertTrue(customer.hasContacts());
        assertTrue(primaryContact.getIsPrimary());

        // Verify primary contact
        var primary = customer.getPrimaryContact();
        assertTrue(primary.isPresent());
        assertEquals("John Doe", primary.get().getFullName());
    }

    @Test
    @DisplayName("Should handle multiple contact persons with one primary")
    void shouldHandleMultipleContactPersonsWithOnePrimary() {
        // Add primary contact
        customer.addContactPerson(primaryContact);

        // Add secondary contact
        customer.addContactPerson(secondaryContact);

        assertEquals(2, customer.getContactCount());

        // Verify only one primary contact
        long primaryCount = customer.getContacts().stream()
                .mapToLong(contact -> contact.getIsPrimary() ? 1 : 0)
                .sum();
        assertEquals(1, primaryCount);

        // Verify secondary contacts
        var secondaryContacts = customer.getSecondaryContacts();
        assertEquals(1, secondaryContacts.size());
        assertEquals("Jane Smith", secondaryContacts.get(0).getFullName());
    }

    @Test
    @DisplayName("Should change primary contact")
    void shouldChangePrimaryContact() {
        customer.addContactPerson(primaryContact);
        customer.addContactPerson(secondaryContact);

        // Change primary contact
        customer.setPrimaryContact(secondaryContact);

        // Verify new primary
        var primary = customer.getPrimaryContact();
        assertTrue(primary.isPresent());
        assertEquals("Jane Smith", primary.get().getFullName());
        assertTrue(secondaryContact.getIsPrimary());
        assertFalse(primaryContact.getIsPrimary());
    }

    @Test
    @DisplayName("Should remove contact person and handle primary reassignment")
    void shouldRemoveContactPersonAndHandlePrimaryReassignment() {
        customer.addContactPerson(primaryContact);
        customer.addContactPerson(secondaryContact);

        // Remove primary contact
        customer.removeContactPerson(primaryContact);

        assertEquals(1, customer.getContactCount());

        // Secondary contact should become primary
        assertTrue(secondaryContact.getIsPrimary());
        var primary = customer.getPrimaryContact();
        assertTrue(primary.isPresent());
        assertEquals("Jane Smith", primary.get().getFullName());
    }

    @Test
    @DisplayName("Should throw exception when setting invalid primary contact")
    void shouldThrowExceptionWhenSettingInvalidPrimaryContact() {
        ContactPerson externalContact = new ContactPerson();
        externalContact.setFirstName("External");
        externalContact.setLastName("Contact");

        assertThrows(IllegalArgumentException.class, () -> {
            customer.setPrimaryContact(externalContact);
        });
    }

    @Test
    @DisplayName("Should generate correct display name")
    void shouldGenerateCorrectDisplayName() {
        assertEquals("ACME Corporation (CUST-2024-0001)", customer.getDisplayName());

        customer.setCustomerNumber(null);
        assertEquals("ACME Corporation", customer.getDisplayName());
    }

    @Test
    @DisplayName("Should validate complete address")
    void shouldValidateCompleteAddress() {
        assertTrue(customer.hasCompleteAddress());

        customer.setPrimaryAddress(null);
        assertFalse(customer.hasCompleteAddress());

        Address incompleteAddress = new Address("", "Berlin", "10115", "Germany");
        customer.setPrimaryAddress(incompleteAddress);
        assertFalse(customer.hasCompleteAddress());
    }
}