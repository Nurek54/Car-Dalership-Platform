package salon.financing.application.domain.model.financing;

/**
 * Obiekt wartości: rozłączne odwołanie do klienta z Kontekstu Sprzedaży i CRM (Rysunek 43).
 * Finansowanie ma własny model identyfikatora klienta (każdy Bounded Context definiuje swój).
 */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
