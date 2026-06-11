package salon.sales.infrastructure.messaging;

import salon.catalog.domain.event.CatalogUpdatedEvent;
import salon.catalog.domain.event.SpecificationCompletedEvent;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Katalogu i Konfiguratora
 * w Kontekście Sprzedaży (komunikacja wg kanwy: SpecificationCompleted, CatalogUpdated).
 *
 * UC-CRM-02, warunek wstępny / krok 1: po odebraniu SpecificationCompleted system
 * powiadamia Handlowca o nowej specyfikacji oczekującej na ofertowanie — samą ofertę
 * Handlowiec generuje przez port IssueProformaUseCase.
 */
public class CatalogEventSubscriberAdapter {

    /** UC-CRM-02, krok 1: nowa kompletna specyfikacja -> powiadomienie Handlowca. */
    public void handleSpecificationCompleted(SpecificationCompletedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.specificationId() == null || event.specificationId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator specyfikacji jest wymagany");
        }
        System.out.println("[CatalogEventSubscriberAdapter] Specyfikacja " + event.specificationId()
                + " gotowa do ofertowania — powiadamiam Handlowca.");
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
