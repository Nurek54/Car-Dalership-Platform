package salon.logistics.application.port.in;

import java.util.List;

/**
 * Port wejściowy (driving) — synchronizacja lokalnej kopii specyfikacji
 * w Kontekście Inwentarza i Logistyki (event-carried state transfer).
 *
 * Wołany przez adaptery zdarzeniowe:
 *  - CatalogEventSubscriberAdapter (SpecificationCompleted -> registerSpecification),
 *  - SalesEventSubscriberAdapter (OrderPlaced -> linkOrderToSpecification).
 */
public interface SynchronizeSpecificationUseCase {

    /** Zapamiętanie kodów wyposażenia skompletowanej specyfikacji (zdarzenie z Katalogu). */
    void registerSpecification(String specificationId, List<String> optionCodes);

    /** Powiązanie zamówienia ze specyfikacją (zdarzenie OrderPlaced ze Sprzedaży). */
    void linkOrderToSpecification(String orderId, String specificationId);
}
