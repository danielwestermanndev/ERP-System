package com.dwestermann.erp.customer.domain;

import com.dwestermann.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "customers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"email", "tenant_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {

    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(name = "email")
    private String email;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    @Column(name = "phone")
    private String phone;

    @Size(max = 20, message = "Customer number must not exceed 20 characters")
    @Column(name = "customer_number", unique = true)
    private String customerNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CustomerType type = CustomerType.B2B;

    @Valid
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "address_street")),
            @AttributeOverride(name = "city", column = @Column(name = "address_city")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "address_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "address_country"))
    })
    private Address primaryAddress;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ContactPerson> contacts = new ArrayList<>();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Domain Logic Methods

    /**
     * Activate the customer
     */
    public void activate() {
        this.status = CustomerStatus.ACTIVE;
    }

    /**
     * Deactivate the customer (soft delete)
     */
    public void deactivate() {
        this.status = CustomerStatus.INACTIVE;
    }

    /**
     * Archive the customer (cannot be reactivated)
     */
    public void archive() {
        this.status = CustomerStatus.ARCHIVED;
    }

    /**
     * Check if customer is active
     */
    public boolean isActive() {
        return CustomerStatus.ACTIVE.equals(this.status);
    }

    /**
     * Check if customer is archived
     */
    public boolean isArchived() {
        return CustomerStatus.ARCHIVED.equals(this.status);
    }

    /**
     * Add a contact person to the customer
     */
    public void addContactPerson(ContactPerson contactPerson) {
        if (contactPerson == null) {
            throw new IllegalArgumentException("Contact person cannot be null");
        }

        contactPerson.setCustomer(this);
        this.contacts.add(contactPerson);

        // If this is the first contact person, make it primary
        if (this.contacts.size() == 1) {
            contactPerson.markAsPrimary();
        }
    }

    /**
     * Remove a contact person from the customer
     */
    public void removeContactPerson(ContactPerson contactPerson) {
        if (contactPerson != null) {
            boolean wasPrimary = contactPerson.getIsPrimary();
            this.contacts.remove(contactPerson);
            contactPerson.setCustomer(null);

            // If removed contact was primary, make first remaining contact primary
            if (wasPrimary && !this.contacts.isEmpty()) {
                this.contacts.get(0).markAsPrimary();
            }
        }
    }

    /**
     * Set a contact person as primary
     */
    public void setPrimaryContact(ContactPerson contactPerson) {
        if (contactPerson == null || !this.contacts.contains(contactPerson)) {
            throw new IllegalArgumentException("Contact person must belong to this customer");
        }

        // Remove primary flag from all contacts
        this.contacts.forEach(ContactPerson::markAsSecondary);

        // Set new primary contact
        contactPerson.markAsPrimary();
    }

    /**
     * Get the primary contact person
     */
    public Optional<ContactPerson> getPrimaryContact() {
        return this.contacts.stream()
                .filter(ContactPerson::getIsPrimary)
                .findFirst();
    }

    /**
     * Get all non-primary contact persons
     */
    public List<ContactPerson> getSecondaryContacts() {
        return this.contacts.stream()
                .filter(contact -> !contact.getIsPrimary())
                .toList();
    }

    /**
     * Check if customer has any contact persons
     */
    public boolean hasContacts() {
        return !this.contacts.isEmpty();
    }

    /**
     * Get contact count
     */
    public int getContactCount() {
        return this.contacts.size();
    }

    /**
     * Generate customer display name for UI
     */
    public String getDisplayName() {
        if (customerNumber != null && !customerNumber.trim().isEmpty()) {
            return String.format("%s (%s)", name, customerNumber);
        }
        return name;
    }

    /**
     * Check if customer has complete address
     */
    public boolean hasCompleteAddress() {
        return primaryAddress != null
                && primaryAddress.getStreet() != null && !primaryAddress.getStreet().trim().isEmpty()
                && primaryAddress.getCity() != null && !primaryAddress.getCity().trim().isEmpty()
                && primaryAddress.getPostalCode() != null && !primaryAddress.getPostalCode().trim().isEmpty()
                && primaryAddress.getCountry() != null && !primaryAddress.getCountry().trim().isEmpty();
    }

    /**
     * Business rule validation before persistence
     */
    @PrePersist
    @PreUpdate
    private void validateBusinessRules() {
        // Ensure exactly one primary contact if contacts exist
        long primaryCount = contacts.stream()
                .mapToLong(contact -> contact.getIsPrimary() ? 1 : 0)
                .sum();

        if (!contacts.isEmpty() && primaryCount != 1) {
            throw new IllegalStateException("Customer must have exactly one primary contact person");
        }
    }
}