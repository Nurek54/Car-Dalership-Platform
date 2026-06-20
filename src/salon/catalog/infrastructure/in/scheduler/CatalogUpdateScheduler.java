package salon.catalog.infrastructure.in.scheduler;

import salon.catalog.application.port.in.UpdateCatalog;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ADAPTER WEJŚCIOWY (zadanie cykliczne) – „CronJob” z diagramu.
 *
 * Okresowo wyzwala automatyczną aktualizację cennika i katalogu (UC-KON-02) przez
 * port wejściowy {@link UpdateCatalog}. Adapter nie zawiera logiki biznesowej –
 * jedynie inicjuje przypadek użycia.
 */
@Component
public class CatalogUpdateScheduler {

    private final UpdateCatalog updateCatalog;

    public CatalogUpdateScheduler(UpdateCatalog updateCatalog) {
        this.updateCatalog = updateCatalog;
    }

    /** Codziennie o 2:00 – sprawdzenie i pobranie nowego pakietu z systemu importera. */
    @Scheduled(cron = "0 0 2 * * *")
    public void triggerCatalogUpdate() {
        updateCatalog.update();
    }
}
