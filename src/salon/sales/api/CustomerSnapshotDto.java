package salon.sales.api;

/**
 * Published Language of the Sales and CRM Context — a snapshot of buyer data
 * made available to other contexts (e.g. Billing) without exposing the aggregates
 * domenowych ({@code Customer}, {@code Offer}, {@code Order}).
 *
 * It contains only the data needed by consumers; client contexts translate
 * this DTO into their own value objects in their ACLs.
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
