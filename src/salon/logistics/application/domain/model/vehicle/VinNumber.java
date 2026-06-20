package salon.logistics.application.domain.model.vehicle;

/**
 * Obiekt wartości: numer VIN — naturalny, niezmienny identyfikator fizycznego pojazdu.
 * Zgodnie z założeniem kanwy skan VIN jest bezbłędny i pokrywa się z danymi cyfrowymi
 * zamówienia produkcyjnego.
 */
public record VinNumber(String value) {

    public VinNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("VIN must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
