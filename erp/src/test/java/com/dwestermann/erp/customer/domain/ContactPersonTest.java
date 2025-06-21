package com.dwestermann.erp.customer.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ContactPersonTest {

    private ContactPerson contactPerson;

    @BeforeEach
    void setUp() {
        contactPerson = new ContactPerson();
        contactPerson.setFirstName("John");
        contactPerson.setLastName("Doe");
        contactPerson.setEmail("john.doe@example.com");
        contactPerson.setPhone("+49 30 12345678");
        contactPerson.setPosition("Manager");
        contactPerson.setIsPrimary(true);
    }

    @Test
    @DisplayName("Should create contact person with all fields")
    void shouldCreateContactPersonWithAllFields() {
        assertEquals("John", contactPerson.getFirstName());
        assertEquals("Doe", contactPerson.getLastName());
        assertEquals("john.doe@example.com", contactPerson.getEmail());
        assertEquals("+49 30 12345678", contactPerson.getPhone());
        assertEquals("Manager", contactPerson.getPosition());
        assertTrue(contactPerson.getIsPrimary());
    }

    @Test
    @DisplayName("Should generate full name")
    void shouldGenerateFullName() {
        assertEquals("John Doe", contactPerson.getFullName());
    }

    @Test
    @DisplayName("Should mark as primary and secondary")
    void shouldMarkAsPrimaryAndSecondary() {
        contactPerson.markAsSecondary();
        assertFalse(contactPerson.getIsPrimary());

        contactPerson.markAsPrimary();
        assertTrue(contactPerson.getIsPrimary());
    }

    @Test
    @DisplayName("Should check email and phone availability")
    void shouldCheckEmailAndPhoneAvailability() {
        assertTrue(contactPerson.hasEmail());
        assertTrue(contactPerson.hasPhone());

        contactPerson.setEmail(null);
        contactPerson.setPhone("");

        assertFalse(contactPerson.hasEmail());
        assertFalse(contactPerson.hasPhone());
    }
}