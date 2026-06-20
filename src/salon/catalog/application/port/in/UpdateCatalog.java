package salon.catalog.application.port.in;

/**
 * PORT WEJŚCIOWY (kontrakt) – „UpdateCatalog” z diagramu portów i adapterów.
 *
 * Publikuje usługę automatycznej aktualizacji cennika i katalogu (UC-KON-02).
 * Wywoływany przez adapter wejściowy CronJob (zadanie cykliczne) lub przez zdarzenie
 * integracyjne z systemu producenta/importera.
 */
public interface UpdateCatalog {

    /**
     * Pobiera, tłumaczy (ACL), waliduje i zapisuje nowy katalog, archiwizując bieżący,
     * a następnie emituje CatalogUpdated. W razie błędu emituje CatalogUpdateFailed.
     */
    void update();
}
