package salon.sales.api;

import salon.common.model.Money;

/**
 * Published Language Kontekstu Sprzedaży i CRM — migawka oferty źródłowej zamówienia
 * udostępniana innym kontekstom (np. Finansowaniu) bez ujawniania agregatu {@code Offer}.
 *
 * {@link Money} pochodzi ze Wspólnego Jądra (Shared Kernel), więc może bezpiecznie
 * przekraczać granice kontekstów.
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
