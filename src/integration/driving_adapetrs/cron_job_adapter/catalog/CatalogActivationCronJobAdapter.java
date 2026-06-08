package integration.driving_adapetrs.cron_job_adapter.catalog;

import salon.catalog.application.service.CatalogAppService;

/**
 * Adapter sterujący (driving) — zadanie cykliczne aktywacji zaplanowanych cenników (UC-KAT-02).
 *
 * W kodzie produkcyjnym metoda ma nad sobą np. @Scheduled(cron = "0 0 1 * * ?") i jest beanem Springa.
 * Tu jest cienko: jedynie deleguje do warstwy aplikacji i — co kluczowe dla zadań w tle —
 * SAM łapie wyjątki i je loguje, żeby błąd nie zabił wątku Schedulera.
 */
public class CatalogActivationCronJobAdapter {

    private final CatalogAppService catalogAppService;

    public CatalogActivationCronJobAdapter(CatalogAppService catalogAppService) {
        if (catalogAppService == null) {
            throw new IllegalArgumentException("catalogAppService must not be null.");
        }
        this.catalogAppService = catalogAppService;
    }

    // Wyzwalane przez harmonogram: znajdź cenniki SCHEDULED i je aktywuj.
    public void activateScheduledCatalogsJob() {
        try {
            catalogAppService.activatePendingCatalogs();
        } catch (Exception e) {
            // Połykamy i logujemy — inaczej Spring Scheduler ubije wątek i zatrzyma inne zadania.
            System.err.println("[CatalogActivationCronJobAdapter] Aktywacja cenników nie powiodła się: "
                    + e.getMessage());
        }
    }
}
