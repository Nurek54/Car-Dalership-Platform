package salon.sales.api;

import salon.common.model.Money;

/**
 * Published Language of the Sales and CRM Context — a snapshot of the order's source offer
 * made available to other contexts (e.g. Financing) without exposing the {@code Offer} aggregate.
 *
 * {@link Money} comes from the Shared Kernel, so it can safely
 * cross context boundaries.
 */
public record OfferSnapshotDto(String offerId, Money finalPrice) {

    public OfferSnapshotDto {
        if (offerId == null || offerId.isBlank()) {
            throw new IllegalArgumentException("offerId must not be null or blank.");
        }
        if (finalPrice == null) {
            throw new IllegalArgumentException("finalPrice must not be null.");
        }
    }
}
