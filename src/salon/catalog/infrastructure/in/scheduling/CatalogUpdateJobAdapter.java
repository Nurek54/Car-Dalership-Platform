package salon.catalog.infrastructure.in.scheduling;

import salon.catalog.application.port.in.UpdateCatalog;

/**
 * Adapter sterujacy (driving) — cykliczna synchronizacja cennika/katalogu (UC-KON-02),
 * PDF rozdz. 3.4.3 ("CatalogUpdateJobAdapter"). Techniczny adapter asynchroniczny: w kodzie
 * produkcyjnym metoda ma nad soba np. @Scheduled(cron = "0 0 3 * * ?") i pobiera parametry
 * nowej bazy modelowej z systemu importera. Adapter sam lapie wyjatki, aby awaria pojedynczego
 * uruchomienia nie zatrzymala calego Schedulera.
 */
public class CatalogUpdateJobAdapter {

    private final UpdateCatalog updateCatalog;

    public CatalogUpdateJobAdapter(UpdateCatalog updateCatalog) {
        if (updateCatalog == null) {
            throw new IllegalArgumentException("updateCatalog must not be null.");
        }
        this.updateCatalog = updateCatalog;
    }

    /** Wyzwalane przez harmonogram: publikacja nowej wersji cennika i archiwizacja poprzedniej. */
    public void synchronizeCatalogJob(String modelYear, String previousCatalogId) {
        try {
            updateCatalog.publishNewCatalogVersion(modelYear, previousCatalogId);
        } catch (Exception e) {
            System.err.println("[CatalogUpdateJobAdapter] Synchronizacja cennika nie powiodla sie: "
                    + e.getMessage());
        }
    }
}
