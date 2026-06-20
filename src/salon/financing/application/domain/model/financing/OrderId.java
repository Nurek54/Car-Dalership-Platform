package salon.financing.application.domain.model.financing;

/**
 * Obiekt wartości: rozłączne odwołanie do zamówienia z Kontekstu Sprzedaży (Rysunek 43).
 * Każdy Kontekst Ograniczony ma własny model identyfikatora — Finansowanie odwołuje się
 * do zamówienia wyłącznie przez tę wartość.
 */
public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
