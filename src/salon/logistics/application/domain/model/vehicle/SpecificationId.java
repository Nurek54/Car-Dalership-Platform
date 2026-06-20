package salon.logistics.application.domain.model.vehicle;

/**
 * Obiekt wartości: rozłączne odwołanie do specyfikacji pojazdu z Kontekstu Katalogu.
 * Inwentarz nie zna modelu Katalogu — przechowuje wyłącznie identyfikator i (lokalnie)
 * kody wyposażenia dostarczone zdarzeniem (event-carried state transfer).
 */
public record SpecificationId(String value) {

    public SpecificationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SpecificationId must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
