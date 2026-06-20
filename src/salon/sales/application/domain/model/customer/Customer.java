package salon.sales.application.domain.model.customer;

import salon.sales.application.domain.exception.InvalidTaxIdException;

/**
 * Aggregate Root: Klient kontekstu Sprzedaży i CRM.
 *
 * Model wg docs/Agregate/Sales/customer-offer-order.md oraz
 * docs/Architecture/SalesArchitecture.md:
 *  - tożsamość: {@link CustomerId},
 *  - dane: fullName, nip, {@link Address}, {@link ContactData},
 *  - operacje: {@link #updateContactDetails(ContactData)}, {@link #verifyTaxId()}.
 *
 * Obiekty wartości (Address, ContactData) są niemutowalne — każda zmiana to
 * podmiana całego obiektu przez agregat (patrz value-object-audit.md).
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
     * Aktualizacja danych kontaktowych — podmiana całego obiektu wartości.
     */
    public void updateContactDetails(ContactData newContact) {
        if (newContact == null) {
            throw new IllegalArgumentException("newContact must not be null.");
        }
        this.contact = newContact;
    }

    /**
     * Weryfikacja numeru NIP (uproszczona): NIP musi istnieć i mieć 10 cyfr.
     * W kodzie produkcyjnym zwykle integracja z rejestrem (np. VIES/GUS).
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

    public CustomerId getId() {
        return this.id;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getNip() {
        return this.nip;
    }

    public Address getAddress() {
        return this.address;
    }

    public ContactData getContact() {
        return this.contact;
    }

    public boolean isTaxIdVerified() {
        return this.taxIdVerified;
    }
}
