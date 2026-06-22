package salon.sales.application.domain.model.customer;

/**
 * Value Object (Figure 23) — the customer's contact details (email, phone).
 * Exposes phone() and the alias phoneNumber() used by the tests.
 */
public record ContactData(String email, String phone) {

    public ContactData {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank.");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("phone must not be blank.");
        }
    }

    /** Alias for {@link #phone()}. */
    public String phoneNumber() {
        return phone;
    }
}
