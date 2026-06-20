package salon.sales.application.port.out;

import salon.common.model.Money;

/**
 * Port wyjściowy (driven) Kontekstu Sprzedaży — synchroniczne zapytanie o cenę
 * katalogową specyfikacji (integracja HTTP z modułem Katalogu).
 *
 * Reguła odwrócenia zależności (D z SOLID): to KONTEKST SPRZEDAŻY definiuje, czego
 * potrzebuje od Katalogu (własny port), a adapter w warstwie infrastruktury realizuje
 * ten kontrakt. Sprzedaż nie zależy już od portu repozytorium Katalogu.
 *
 * Uwaga: w trybie zdarzeniowym (event-carried state transfer) wycena dociera
 * asynchronicznie przez {@link SpecificationPriceReadModelPort} ze zdarzenia
 * SpecificationCompleted; ten port pozostaje dla integracji synchronicznej, gdy jest wymagana.
 */
public interface CatalogPriceQueryPort {

    Money getSpecificationPrice(String specificationId);
}
