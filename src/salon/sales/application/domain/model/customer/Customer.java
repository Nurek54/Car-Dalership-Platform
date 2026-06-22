package salon.sales.application.domain.model.customer;

import salon.sales.application.domain.exception.InvalidTaxIdException;

/**
 * AGGREGATE ROOT (Figure 23) — Customer.
 *
 * Holds the customer's identity and contact/tax data used to prepare offers and orders.
 * Business rules (tax-id validation, contact updates) live here. The tax id (NIP) is optional —
 * a private individual may have none — but when present it must be a valid 10-digit number.
 */
public class Customer {

    private final CustomerId id;
    private final String fullName;
    private final String nip;
    private Address address;
    private ContactData contact;

    public Customer(CustomerId id, String fullName, String nip, Address address, ContactData contact) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be blank.");
        }
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null.");
        }
        this.id = id;
        this.fullName = fullName;
        this.nip = nip; // optional — an individual may have no tax id
        this.address = address;
        this.contact = contact;
    }

    /** Updates the mutable contact details, keeping the identity intact. */
    public void updateContactDetails(ContactData contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null.");
        }
        this.contact = contact;
    }

    /** Updates both the address and the contact details. */
    public void updateContactDetails(Address address, ContactData contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null.");
        }
        this.address = address;
        this.contact = contact;
    }

    /** Validates the tax identifier (NIP): exactly 10 digits when present. */
    public void verifyTaxId() {
        if (this.nip == null || !this.nip.matches("\\d{10}")) {
            throw new InvalidTaxIdException("Provided NIP format is invalid: " + this.nip);
        }
    }

    // ----- JavaBean-style getters -----

    public CustomerId getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNip() {
        return nip;
    }

    public Address getAddress() {
        return address;
    }

    public ContactData getContact() {
        return contact;
    }

    // ----- short, record-style accessors (tests) -----

    public CustomerId id() {
        return id;
    }

    public String fullName() {
        return fullName;
    }

    public String nip() {
        return nip;
    }

    public Address address() {
        return address;
    }

    public ContactData contact() {
        return contact;
    }
}
