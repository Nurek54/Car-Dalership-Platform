package salon.billing.domain.model.document;

/**
 * Value Object: dane wystawcy (salonu).
 */
public record SellerDetails(String name, String nip) {

    public SellerDetails {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Seller name is required.");
        }
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("Seller NIP is required.");
        }
    }
}
