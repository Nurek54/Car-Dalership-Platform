package salon.catalog.infrastructure.in.scheduler;

import salon.catalog.application.port.in.UpdateCatalog;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CatalogUpdateScheduler {

    private final UpdateCatalog updateCatalog;

    public CatalogUpdateScheduler(UpdateCatalog updateCatalog) {
        this.updateCatalog = updateCatalog;
    }

    
    @Scheduled(cron = "0 0 2 * * *")
    public void triggerCatalogUpdate() {
        updateCatalog.update();
    }
}
