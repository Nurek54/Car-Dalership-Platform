package integration.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.catalog.application.port.in.UpdateCatalog;
import salon.catalog.infrastructure.in.scheduler.CatalogUpdateScheduler;

import static org.mockito.Mockito.verify;

/** Integracja adaptera wejściowego CronJob — okresowe wyzwalanie aktualizacji cennika (UC-KON-02). */
@SpringBootTest(classes = CatalogUpdateScheduler.class)
class CatalogUpdateSchedulerTest {

    @Autowired private CatalogUpdateScheduler scheduler;
    @MockBean private UpdateCatalog updateCatalog;

    @Test
    void shouldTriggerCatalogUpdateJob() {
        // Zegar systemowy (Spring Scheduler) odpala metodę w nocy
        scheduler.triggerCatalogUpdate();

        // Adapter zleca wykonanie pracy do portu wejściowego UpdateCatalog
        verify(updateCatalog).update();
    }
}
