package salon.sales.application.domain.model.customer;

import salon.sales.application.domain.exception.InvalidTaxIdException;

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
        this.nip = nip; 
        this.address = address;
        this.contact = contact;
    }

    
    public void updateContactDetails(ContactData contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null.");
        }
        this.contact = contact;
    }

    
    public void updateContactDetails(Address address, ContactData contact) {
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null.");
        }
        this.address = address;
        this.contact = contact;
    }

    
    public void verifyTaxId() {
        if (this.nip == null || !this.nip.matches("\\d{10}")) {
            throw new InvalidTaxIdException("Provided NIP format is invalid: " + this.nip);
        }
    }

    

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
