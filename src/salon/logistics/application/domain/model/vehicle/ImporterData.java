package salon.logistics.application.domain.model.vehicle;

/**
 * Dane tożsamości pojazdu przetłumaczone przez ACL z systemu Importera (UC-INW-01, krok 2).
 * Czysty, niemutowalny obiekt domenowy — agregat nie zna formatu API Importera.
 */
public record ImporterData(String vin) {

    public ImporterData {
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("ImporterData.vin must not be blank.");
        }
    }
}
