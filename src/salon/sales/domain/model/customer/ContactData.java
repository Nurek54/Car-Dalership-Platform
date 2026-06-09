package salon.sales.domain.model.customer;

/**
 * Value Object: dane kontaktowe Klienta. Niemutowalny (rekord) — patrz
 * docs/Agregate/Guidelines/value-object-audit.md.
 *
 * Zmiana danych kontaktowych odbywa się przez agregat {@code Customer}
 * (podmiana całego obiektu wartości), nigdy przez mutację tego rekordu.
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
}
