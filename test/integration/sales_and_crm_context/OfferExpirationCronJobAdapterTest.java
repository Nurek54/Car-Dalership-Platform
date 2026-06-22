package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.sales.infrastructure.in.cron.OfferExpirationCronJobAdapter;
import salon.sales.application.service.SalesService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = OfferExpirationCronJobAdapter.class)
class OfferExpirationCronJobAdapterTest {

    @Autowired private OfferExpirationCronJobAdapter cronJobAdapter;
    @MockBean private SalesService salesAppService;

    @Test
    void shouldTriggerOfferExpirationJobSuccessfully() {
        // Zegar systemowy (Spring Scheduler) odpala metodę w nocy
        cronJobAdapter.expireOldOffersJob();

        // AppService dostaje polecenie sprawdzenia dat wszystkich ofert
        verify(salesAppService).expireOutdatedOffers();
    }

    @Test
    void shouldContinueRunningEvenIfAppServiceThrowsException() {
        // AppService napotyka błąd
        doThrow(new RuntimeException("Database timeout")).when(salesAppService).expireOutdatedOffers();

        // Adapter przechwytuje błąd zadania okresowego, loguje go
        // i kończy bez wysadzania całego procesu, aby cron odpalił się ponownie później
        assertDoesNotThrow(() -> cronJobAdapter.expireOldOffersJob());
    }
}