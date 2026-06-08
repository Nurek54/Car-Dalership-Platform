package salon.logistics.domain.model.vehicle;

/**
 * Value Object: numer VIN — globalny identyfikator fizycznego egzemplarza (UC-INW-01).
 * ZALOZENIE: walidujemy tylko "nie pusty" (testy często używają skróconych, sztucznych VIN-ów).
 */
public record VinNumber(String value) {

    public VinNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("VinNumber must not be blank.");
        }
    }
}
