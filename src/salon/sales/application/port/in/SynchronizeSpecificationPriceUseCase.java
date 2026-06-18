package salon.sales.application.port.in;

import salon.common.model.Money;

/**
 * Port wejściowy (driving) — synchronizacja lokalnej wyceny specyfikacji w Kontekście
 * Sprzedaży na podstawie zdarzenia SpecificationCompleted z Katalogu (UC-CRM-02,
 * warunek wstępny). Wywoływany przez adapter subskrybenta zdarzeń (CatalogEventSubscriberAdapter).
 */
public interface SynchronizeSpecificationPriceUseCase {

    /** Zapis wyceny katalogowej specyfikacji (ze zdarzenia SpecificationCompleted). */
    void registerSpecificationPrice(String specificationId, Money price);
}
