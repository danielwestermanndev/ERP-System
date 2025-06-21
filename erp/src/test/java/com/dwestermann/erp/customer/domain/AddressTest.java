package com.dwestermann.erp.customer.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    @Test
    @DisplayName("Should create address with all fields")
    void shouldCreateAddressWithAllFields() {
        Address address = new Address("Musterstraße 1", "Berlin", "10115", "Germany");

        assertEquals("Musterstraße 1", address.getStreet());
        assertEquals("Berlin", address.getCity());
        assertEquals("10115", address.getPostalCode());
        assertEquals("Germany", address.getCountry());
    }

    @Test
    @DisplayName("Should generate full address string")
    void shouldGenerateFullAddressString() {
        Address address = new Address("Musterstraße 1", "Berlin", "10115", "Germany");

        String fullAddress = address.getFullAddress();
        assertEquals("Musterstraße 1, 10115 Berlin, Germany", fullAddress);
    }

    @Test
    @DisplayName("Should identify German addresses")
    void shouldIdentifyGermanAddresses() {
        Address germanAddress1 = new Address("Musterstraße 1", "Berlin", "10115", "Germany");
        Address germanAddress2 = new Address("Teststraße 2", "München", "80331", "Deutschland");
        Address nonGermanAddress = new Address("Main Street 1", "New York", "10001", "USA");

        assertTrue(germanAddress1.isGermanAddress());
        assertTrue(germanAddress2.isGermanAddress());
        assertFalse(nonGermanAddress.isGermanAddress());
    }
}