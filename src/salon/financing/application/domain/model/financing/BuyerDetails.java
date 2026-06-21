package salon.financing.application.domain.model.financing;

/**
 * Value object (Figure 43) – buyer data needed for the bank application.
 * Populated by the ACL based on a snapshot from the Sales Context (CustomerSnapshotDto).
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
