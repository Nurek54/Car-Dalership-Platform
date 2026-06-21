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
        // The system clock (Spring Scheduler) fires the method at night
        cronJobAdapter.expireOldOffersJob();

        // The AppService is notified to check the dates of all offers
        verify(salesAppService).expireOutdatedOffers();
    }

    @Test
    void shouldContinueRunningEvenIfAppServiceThrowsException() {
        // The AppService encounters an error
        doThrow(new RuntimeException("Database timeout")).when(salesAppService).expireOutdatedOffers();

        // The adapter catches the periodic task's error, logs it
        // and finishes without blowing up the whole process, so that the cron will fire again later
        assertDoesNotThrow(() -> cronJobAdapter.expireOldOffersJob());
    }
}