package salon.sales.infrastructure.messaging;

import salon.sales.application.service.OfferAppService;
import salon.shared.event.CatalogVersionPublishedEvent;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Katalogu w Kontekście Sprzedaży (UC-KON-02).
 *
 * Po publikacji nowej wersji cennika unieważnia oferty oparte o starsze katalogi. Waliduje wejście
 * (ochrona przed uszkodzonymi danymi z kolejki); błędy warstwy aplikacji przepuszcza wyżej (DLQ).
 */
public class CatalogEventSubscriberAdapter {

    private final OfferAppService offerAppService;

    public CatalogEventSubscriberAdapter(OfferAppService offerAppService) {
        if (offerAppService == null) {
            throw new IllegalArgumentException("offerAppService must not be null.");
        }
        this.offerAppService = offerAppService;
    }

    public void handleCatalogVersionPublishedEvent(CatalogVersionPublishedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.catalogId() == null || event.catalogId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator katalogu (catalogId) jest wymagany");
        }
        offerAppService.invalidateOffersForOlderCatalogs(event.catalogId());
    }
}
