package salon.sales.application.domain.model.customer;

import salon.sales.application.domain.exception.InvalidTaxIdException;

/**
 * Aggregate Root: the Customer of the Sales and CRM context.
 *
 * Model per docs/Agregate/Sales/customer-offer-order.md and
 * docs/Architecture/SalesArchitecture.md:
 *  - identity: {@link CustomerId},
 *  - data: fullName, nip, {@link Address}, {@link ContactData},
 *  - operacje: {@link #updateContactDetails(ContactData)}, {@link #verifyTaxId()}.
 *
 * Value objects (Address, ContactData) are immutable — every change is
 * a replacement of the whole object by the aggregate (see value-object-audit.md).
 */
public class Customer {

    private final CustomerId id;
    private String fullName;
    private String nip;
    private Address address;
    private ContactData contact;
    private boolean taxIdVerified;

    public Customer(CustomerId id, String fullName, String nip, Address address, ContactData contact) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be blank.");
        }
        if (address == null) {
            throw new IllegalArgumentException("address must not be null.");
        }
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null.");
        }
        this.id = id;
        this.fullName = fullName;
        this.nip = nip;
        this.address = address;
        this.contact = contact;
        this.taxIdVerified = false;
    }

    /**
     * Updating the contact details — replacement of the whole value object.
     */
    public void updateContactDetails(ContactData newContact) {
        if (newContact == null) {
            throw new IllegalArgumentException("newContact must not be null.");
        }
        this.contact = newContact;
    }

    /**
     * Tax ID verification (simplified): the tax ID must exist and have 10 digits.
     * In production code this is usually an integration with a registry (e.g. VIES/GUS).
     */
    public void verifyTaxId() {
        if (this.nip == null || this.nip.isBlank()) {
            throw new InvalidTaxIdException(
                    "Provided NIP format is invalid: customer has no NIP to verify.");
        }
        String digits = this.nip.replaceAll("\\s|-", "");
        if (!digits.matches("\\d{10}")) {
            throw new InvalidTaxIdException(
                    "Provided NIP format is invalid: " + this.nip);
        }
        this.taxIdVerified = true;
    }

    public CustomerId id() {
        return this.id;
    }

    public String fullName() {
        return this.fullName;
    }

    public String nip() {
        return this.nip;
    }

    public Address address() {
        return this.address;
    }

    public ContactData contact() {
        return this.contact;
    }

    public boolean isTaxIdVerified() {
        return this.taxIdVerified;
    }
}
