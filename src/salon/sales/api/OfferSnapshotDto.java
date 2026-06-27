package salon.sales.api;

import salon.common.model.Money;

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
