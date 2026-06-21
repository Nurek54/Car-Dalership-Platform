package salon.sales.application.domain.model.customer;

/**
 * Value Object: the Customer's address. Immutable (record) — see
 * docs/Agregate/Guidelines/value-object-audit.md.
 */
public record Address(String street, String postalCode, String city, String country) {

    public Address {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("street must not be blank.");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("postalCode must not be blank.");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("city must not be blank.");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("country must not be blank.");
        }
    }
}
