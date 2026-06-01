package salon.billing.domain.model.document;

// Value Object: dane podatkowe nabywcy.
public record TaxDetails(String buyerName, String nip) {

    public TaxDetails {
        if (buyerName == null || buyerName.isBlank()) {
            throw new IllegalArgumentException("Buyer name (buyerName) is required.");
        }
        // NIP bywa pusty (np. paragon dla osoby fizycznej) — dlatego go nie blokujemy.
    }
}
