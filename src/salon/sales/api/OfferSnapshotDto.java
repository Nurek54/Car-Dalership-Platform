package salon.sales.api;

import salon.common.model.Money;

/**
 * Published Language of the Sales and CRM Context — a snapshot of an order's source offer
 * (final price) exposed to other contexts without revealing the {@code Offer} aggregate.
 *
 * {@link Money} comes from the Shared Kernel, so it may safely cross context boundaries.
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
