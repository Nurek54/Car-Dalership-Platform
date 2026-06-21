package salon.billing.application.domain.model.document;

/**
 * VALUE OBJECT (Class diagram — «ValueObject» SellerDetails): fixed issuer (dealership) data.
 * Immutable; injected into the domain service from configuration (Composition Root).
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
