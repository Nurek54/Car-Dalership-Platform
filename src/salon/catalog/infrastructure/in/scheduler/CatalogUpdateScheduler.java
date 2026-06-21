package salon.catalog.infrastructure.in.scheduler;

import salon.catalog.application.port.in.UpdateCatalog;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * INBOUND ADAPTER (periodic task) – "CronJob" from the diagram.
 *
 * Periodically triggers the automatic update of the price list and catalog (UC-KON-02) via
 * the inbound port {@link UpdateCatalog}. The adapter contains no business logic –
 * it only initiates the use case.
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
