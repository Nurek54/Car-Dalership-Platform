package salon.sales.infrastructure.in.cron;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import salon.sales.application.service.SalesService;

/**
 * INBOUND ADAPTER (Figure 22: CronJob) — periodically drives {@link SalesService#expireOutdatedOffers()}.
 * Any error from the application layer is caught and logged so the periodic task keeps firing later.
 */
@Component
public class OfferExpirationCronJobAdapter {

    private final SalesService salesAppService;

    public OfferExpirationCronJobAdapter(SalesService salesAppService) {
        this.salesAppService = salesAppService;
    }

    /** Scheduled nightly entry point (UC: reject offers past their validity date). */
    @Scheduled(cron = "0 0 2 * * *")
    public void expireOldOffersJob() {
        try {
            this.salesAppService.expireOutdatedOffers();
        } catch (RuntimeException ex) {
            // Swallow & log: the cron must survive a transient failure and fire again next time.
            System.err.println("[OfferExpirationCronJobAdapter] Offer expiration job failed: " + ex.getMessage());
        }
    }
}
