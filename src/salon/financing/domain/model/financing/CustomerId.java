package salon.financing.domain.model.financing;

/**
 * Value Object: lokalny identyfikator klienta w kontekście Finansowania (model rozłączny).
 */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank.");
        }
    }
}
