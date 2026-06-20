package salon.financing.application.domain.model.financing;

/**
 * Obiekt wartości (Rysunek 43) – dane nabywcy potrzebne do wniosku bankowego.
 * Wypełniane przez ACL na podstawie migawki z Kontekstu Sprzedaży (CustomerSnapshotDto).
 */
public record BuyerDetails(String name, String nip) {

    public BuyerDetails {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("nip must not be blank.");
        }
    }
}
