package salon.sales.api;

/**
 * Published Language of the Sales and CRM Context — a snapshot of buyer data exposed to other
 * contexts (e.g. Billing, Financing) without revealing the domain aggregates.
 *
 * Client contexts translate this DTO into their own value objects in their ACL.
 */
public record CustomerSnapshotDto(String fullName, String nip) {

    public CustomerSnapshotDto {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be null or blank.");
        }
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("nip must not be null or blank.");
        }
    }
}
