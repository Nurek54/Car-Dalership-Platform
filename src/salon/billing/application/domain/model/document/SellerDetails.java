package salon.billing.application.domain.model.document;

/**
 * OBIEKT WARTOŚCI (Diagram klas — «ValueObject» SellerDetails): stałe dane wystawcy (salonu).
 * Niemutowalny; wstrzykiwany do usługi dziedziny z konfiguracji (Composition Root).
 */
public record SellerDetails(String name, String nip) {

    public SellerDetails {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("nip must not be blank.");
        }
    }
}
