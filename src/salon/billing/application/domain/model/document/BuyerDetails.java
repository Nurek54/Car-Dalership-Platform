package salon.billing.application.domain.model.document;

/**
 * VALUE OBJECT (Class diagram — «ValueObject» BuyerDetails): buyer data on the invoice.
 *
 * Immutable, models a conceptual whole (name + tax ID). Populated by the ACL based on
 * a snapshot from the Sales Context (CustomerSnapshotDto). The {@link #isCorporate()} operation is
 * a side-effect-free query — it decides the invoice due date (UC-FIR-01/02).
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

    /** Corporate buyer = a valid 10-digit tax ID (longer due date than for an individual). */
    public boolean isCorporate() {
        String digits = this.nip.replaceAll("\\D", "");
        return digits.length() == 10;
    }
}
