package salon.catalog.application.port.out;

import salon.catalog.application.dto.ImportedCatalogData;

/**
 * PORT WYJŚCIOWY – odpytanie zewnętrznego systemu producenta/importera o najnowszy
 * pakiet katalogowy („ImporterACL” → ImporterService).
 *
 * Mapowanie kontekstów: WARSTWA ZAPOBIEGAJĄCA USZKODZENIU (ACL). Adapter
 * implementujący ten port tłumaczy zewnętrzny (obcy) format na model lokalny, zanim
 * dane trafią do warstwy aplikacji – kontekst Katalogu nie zależy od modelu importera.
 */
public interface CatalogImporterPort {

    /**
     * Zwraca dane katalogu już PRZETŁUMACZONE do modelu lokalnego (obiekty wartości
     * dziedziny). Zbudowanie agregatu z tych danych jest zadaniem fabryki w usłudze aplikacji.
     */
    ImportedCatalogData fetchLatestCatalog();
}
