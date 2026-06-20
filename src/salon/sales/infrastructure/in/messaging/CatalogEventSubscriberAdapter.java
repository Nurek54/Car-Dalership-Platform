package salon.sales.infrastructure.in.messaging;

import salon.catalog.application.domain.model.event.CatalogUpdated;
import salon.catalog.application.domain.model.event.SpecificationCompleted;
import salon.common.model.Money;
import salon.sales.application.port.in.SynchronizeSpecificationPriceUseCase;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Katalogu i Konfiguratora
 * w Kontekście Sprzedaży (komunikacja wg kanwy: SpecificationCompleted, CatalogUpdated).
 *
 * Warstwa zapobiegająca uszkodzeniu (ACL): adapter odbiera zdarzenia w języku Katalogu
 * (SpecificationId/Money/CatalogId Katalogu) i TŁUMACZY je na model Sprzedaży
 * (String + salon.common.model.Money) przed wywołaniem portu wejściowego. Dzięki temu
 * dziedzina Sprzedaży nie zależy od modelu Katalogu.
 *
 * UC-CRM-02, warunek wstępny / krok 1: po odebraniu SpecificationCompleted adapter zapisuje
 * wyliczoną cenę katalogową do lokalnego read modelu (event-carried state transfer) i powiadamia
 * Handlowca o nowej specyfikacji oczekującej na ofertowanie.
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
    public void handleSpecificationCompleted(SpecificationCompleted event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        // Tłumaczenie z języka Katalogu na język Sprzedaży (ACL).
        String specificationId = event.specificationId().toString();
        Money price = Money.of(
                event.totalPrice().amount(),
                event.totalPrice().currency().getCurrencyCode());

        this.synchronizeSpecificationPrice.registerSpecificationPrice(specificationId, price);
        System.out.println("[CatalogEventSubscriberAdapter] Specyfikacja " + specificationId
                + " gotowa do ofertowania (cena " + price + ") — powiadamiam Handlowca.");
    }

    /** UC-KON-02: opublikowano nową wersję cennika — informacja dla zespołu sprzedaży. */
    public void handleCatalogUpdated(CatalogUpdated event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        String catalogId = event.catalogId().toString();
        System.out.println("[CatalogEventSubscriberAdapter] Nowa wersja cennika " + catalogId
                + " — nowe oferty będą budowane na zaktualizowanym katalogu.");
    }
}
