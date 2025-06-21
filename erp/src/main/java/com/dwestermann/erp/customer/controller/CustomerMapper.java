// CustomerMapper.java
package com.dwestermann.erp.customer.controller;

import com.dwestermann.erp.customer.domain.Address;
import com.dwestermann.erp.customer.domain.ContactPerson;
import com.dwestermann.erp.customer.domain.Customer;
import com.dwestermann.erp.customer.dto.request.*;
import com.dwestermann.erp.customer.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {

    // Entity to Response mappings

    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setCustomerNumber(customer.getCustomerNumber());
        response.setStatus(customer.getStatus());
        response.setType(customer.getType());
        response.setPrimaryAddress(toAddressResponse(customer.getPrimaryAddress()));
        response.setNotes(customer.getNotes());
        response.setContacts(toContactPersonResponseList(customer.getContacts()));
        response.setPrimaryContact(customer.getPrimaryContact()
                .map(this::toContactPersonResponse)
                .orElse(null));
        response.setContactCount(customer.getContactCount());
        response.setDisplayName(customer.getDisplayName());
        response.setHasCompleteAddress(customer.hasCompleteAddress());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        return response;
    }

    public CustomerSummaryResponse toSummaryResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerSummaryResponse response = new CustomerSummaryResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setCustomerNumber(customer.getCustomerNumber());
        response.setStatus(customer.getStatus());
        response.setType(customer.getType());
        response.setCity(customer.getPrimaryAddress() != null ? customer.getPrimaryAddress().getCity() : null);
        response.setPrimaryContactName(customer.getPrimaryContact()
                .map(ContactPerson::getFullName)
                .orElse(null));
        response.setContactCount(customer.getContactCount());
        response.setDisplayName(customer.getDisplayName());
        response.setCreatedAt(customer.getCreatedAt());

        return response;
    }

    public ContactPersonResponse toContactPersonResponse(ContactPerson contactPerson) {
        if (contactPerson == null) {
            return null;
        }

        ContactPersonResponse response = new ContactPersonResponse();
        response.setId(contactPerson.getId());
        response.setFirstName(contactPerson.getFirstName());
        response.setLastName(contactPerson.getLastName());
        response.setFullName(contactPerson.getFullName());
        response.setEmail(contactPerson.getEmail());
        response.setPhone(contactPerson.getPhone());
        response.setPosition(contactPerson.getPosition());
        response.setIsPrimary(contactPerson.getIsPrimary());
        response.setHasEmail(contactPerson.hasEmail());
        response.setHasPhone(contactPerson.hasPhone());
        response.setCreatedAt(contactPerson.getCreatedAt());
        response.setUpdatedAt(contactPerson.getUpdatedAt());

        return response;
    }

    public AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }

        AddressResponse response = new AddressResponse();
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setPostalCode(address.getPostalCode());
        response.setCountry(address.getCountry());
        response.setFullAddress(address.getFullAddress());
        response.setGermanAddress(address.isGermanAddress());

        return response;
    }

    public List<ContactPersonResponse> toContactPersonResponseList(List<ContactPerson> contacts) {
        if (contacts == null) {
            return null;
        }

        return contacts.stream()
                .map(this::toContactPersonResponse)
                .collect(Collectors.toList());
    }

    public CustomerListResponse toListResponse(Page<Customer> customerPage) {
        if (customerPage == null) {
            return new CustomerListResponse();
        }

        List<CustomerSummaryResponse> customers = customerPage.getContent().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());

        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(customerPage.getNumber());
        pagination.setSize(customerPage.getSize());
        pagination.setTotalElements(customerPage.getTotalElements());
        pagination.setTotalPages(customerPage.getTotalPages());
        pagination.setHasNext(customerPage.hasNext());
        pagination.setHasPrevious(customerPage.hasPrevious());
        pagination.setFirst(customerPage.isFirst());
        pagination.setLast(customerPage.isLast());

        CustomerListResponse response = new CustomerListResponse();
        response.setCustomers(customers);
        response.setPagination(pagination);

        return response;
    }

    // Request to Entity mappings

    public Customer toEntity(CreateCustomerRequest request) {
        if (request == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCustomerNumber(request.getCustomerNumber());
        customer.setType(request.getType());
        customer.setPrimaryAddress(toAddressEntity(request.getPrimaryAddress()));
        customer.setNotes(request.getNotes());

        // Add primary contact if provided
        if (request.getPrimaryContact() != null) {
            ContactPerson primaryContact = toEntity(request.getPrimaryContact());
            customer.addContactPerson(primaryContact);
        }

        return customer;
    }

    public Customer toEntity(UpdateCustomerRequest request) {
        if (request == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setId(request.getId());
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setCustomerNumber(request.getCustomerNumber());
        customer.setStatus(request.getStatus());
        customer.setType(request.getType());
        customer.setPrimaryAddress(toAddressEntity(request.getPrimaryAddress()));
        customer.setNotes(request.getNotes());

        return customer;
    }

    public ContactPerson toEntity(CreateContactPersonRequest request) {
        if (request == null) {
            return null;
        }

        ContactPerson contactPerson = new ContactPerson();
        contactPerson.setFirstName(request.getFirstName());
        contactPerson.setLastName(request.getLastName());
        contactPerson.setEmail(request.getEmail());
        contactPerson.setPhone(request.getPhone());
        contactPerson.setPosition(request.getPosition());
        contactPerson.setIsPrimary(request.getIsPrimary());

        return contactPerson;
    }

    public ContactPerson toEntity(UpdateContactPersonRequest request) {
        if (request == null) {
            return null;
        }

        ContactPerson contactPerson = new ContactPerson();
        contactPerson.setId(request.getId());
        contactPerson.setFirstName(request.getFirstName());
        contactPerson.setLastName(request.getLastName());
        contactPerson.setEmail(request.getEmail());
        contactPerson.setPhone(request.getPhone());
        contactPerson.setPosition(request.getPosition());
        contactPerson.setIsPrimary(request.getIsPrimary());

        return contactPerson;
    }

    public Address toAddressEntity(AddressRequest request) {
        if (request == null) {
            return null;
        }

        return new Address(
                request.getStreet(),
                request.getCity(),
                request.getPostalCode(),
                request.getCountry()
        );
    }

    // Utility mappings

    public List<Customer> toEntityList(List<CreateCustomerRequest> requests) {
        if (requests == null) {
            return null;
        }

        return requests.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<CustomerResponse> toResponseList(List<Customer> customers) {
        if (customers == null) {
            return null;
        }

        return customers.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CustomerSummaryResponse> toSummaryResponseList(List<Customer> customers) {
        if (customers == null) {
            return null;
        }

        return customers.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    // Partial update methods (for PATCH operations - future use)

    public void updateEntityFromRequest(Customer customer, UpdateCustomerRequest request) {
        if (customer == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getCustomerNumber() != null) {
            customer.setCustomerNumber(request.getCustomerNumber());
        }
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }
        if (request.getType() != null) {
            customer.setType(request.getType());
        }
        if (request.getPrimaryAddress() != null) {
            customer.setPrimaryAddress(toAddressEntity(request.getPrimaryAddress()));
        }
        if (request.getNotes() != null) {
            customer.setNotes(request.getNotes());
        }
    }

    public void updateEntityFromRequest(ContactPerson contactPerson, UpdateContactPersonRequest request) {
        if (contactPerson == null || request == null) {
            return;
        }

        if (request.getFirstName() != null) {
            contactPerson.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            contactPerson.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            contactPerson.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            contactPerson.setPhone(request.getPhone());
        }
        if (request.getPosition() != null) {
            contactPerson.setPosition(request.getPosition());
        }
        if (request.getIsPrimary() != null) {
            contactPerson.setIsPrimary(request.getIsPrimary());
        }
    }
}