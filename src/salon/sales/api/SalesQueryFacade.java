package salon.sales.api;

import salon.shared.model.OrderId;

/**
 * Publiczne API (fasada) Kontekstu Sprzedaży i CRM dla zapytań synchronicznych
 * z innych kontekstów. Jedyny — obok zdarzeń domenowych — legalny punkt wejścia
 * do kontekstu Sprzedaży; ukrywa repozytoria, agregaty i nawigację
 * zamówienie -> oferta -> klient.
 *
 * W środowisku rozproszonym kontrakt ten mapuje się 1:1 na endpoint REST
 * modułu CRM.
 */
public interface SalesQueryFacade {

    /**
     * Zwraca migawkę danych nabywcy dla wskazanego zamówienia.
     *
     * @throws IllegalArgumentException gdy orderId jest null
     * @throws IllegalStateException    gdy zamówienie, oferta lub klient nie istnieją
     */
    CustomerSnapshotDto findBuyerForOrder(OrderId orderId);
}
