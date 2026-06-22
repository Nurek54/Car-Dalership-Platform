package salon.sales.application.domain.model.customer;

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
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("nip must not be blank.");
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
    }

    public void updateContactDetails(Address address, ContactData contact) {
        if (address == null) {
            throw new IllegalArgumentException("address must not be null.");
        }
        if (contact == null) {
            throw new IllegalArgumentException("contact must not be null.");
        }
        this.address = address;
        this.contact = contact;
    }

    public void verifyTaxId() {
        if (!this.nip.matches("\\d{10}")) {
            throw new IllegalArgumentException("Invalid NIP (expected 10 digits): " + this.nip);
        }
    }

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
}
