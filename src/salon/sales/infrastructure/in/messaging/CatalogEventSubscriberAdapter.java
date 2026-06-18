package salon.sales.infrastructure.in.messaging;

import salon.catalog.application.domain.event.CatalogUpdatedEvent;
import salon.catalog.application.domain.event.SpecificationCompletedEvent;
import salon.sales.application.port.in.SynchronizeSpecificationPriceUseCase;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Katalogu i Konfiguratora
 * w Kontekście Sprzedaży (komunikacja wg kanwy: SpecificationCompleted, CatalogUpdated).
 *
 * UC-CRM-02, warunek wstępny / krok 1: po odebraniu SpecificationCompleted adapter zapisuje
 * wyliczoną cenę katalogową do lokalnego read modelu (event-carried state transfer) i powiadamia
 * Handlowca o nowej specyfikacji oczekującej na ofertowanie — samą ofertę (z danymi klienta)
 * Handlowiec generuje przez port ReceiveSpecificationUseCase.
 */
public class CatalogEventSubscriberAdapter {

    private final SynchronizeSpecificationPriceUseCase synchronizeSpecificationPrice;

    public CatalogEventSubscriberAdapter(SynchronizeSpecificationPriceUseCase synchronizeSpecificationPrice) {
        if (synchronizeSpecificationPrice == null) {
            throw new IllegalArgumentException("synchronizeSpecificationPrice must not be null.");
        }
        this.synchronizeSpecificationPrice = synchronizeSpecificationPrice;
    }

    /** UC-CRM-02, krok 1: nowa kompletna specyfikacja -> zapis wyceny + powiadomienie Handlowca. */
    public void handleSpecificationCompleted(SpecificationCompletedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.specificationId() == null || event.specificationId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator specyfikacji jest wymagany");
        }
        if (event.price() == null) {
            throw new IllegalArgumentException("Wyliczona cena specyfikacji jest wymagana");
        }
        this.synchronizeSpecificationPrice.registerSpecificationPrice(
                event.specificationId(), event.price());
        System.out.println("[CatalogEventSubscriberAdapter] Specyfikacja " + event.specificationId()
                + " gotowa do ofertowania (cena " + event.price() + ") — powiadamiam Handlowca.");
    }

    /** UC-KON-02: opublikowano nową wersję cennika — informacja dla zespołu sprzedaży. */
    public void handleCatalogUpdated(CatalogUpdatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.catalogId() == null || event.catalogId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator katalogu (catalogId) jest wymagany");
        }
        System.out.println("[CatalogEventSubscriberAdapter] Nowa wersja cennika " + event.catalogId()
                + " — nowe oferty będą budowane na zaktualizowanym katalogu.");
    }
}
