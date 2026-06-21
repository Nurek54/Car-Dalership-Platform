package salon.sales.infrastructure.in.cron;

import org.springframework.stereotype.Component;
import salon.sales.application.service.SalesService;

/**
 * Driving adapter — periodic task for expiring overdue offers.
 *
 * In production code the method is annotated, e.g. @Scheduled(cron = "0 0 3 * * ?") (at night).
 * The adapter delegates to the application layer and catches exceptions itself, so that a single
 * run's failure does not stop the whole Spring Scheduler (the cron will fire again).
 */
@Component
public class OfferExpirationCronJobAdapter {

    private final SalesService salesAppService;

    public OfferExpirationCronJobAdapter(SalesService salesAppService) {
        if (salesAppService == null) {
            throw new IllegalArgumentException("salesAppService must not be null.");
        }
        this.salesAppService = salesAppService;
    }

    // Triggered by the schedule: expired offers drop out of the active flow.
    public void expireOldOffersJob() {
        try {
            salesAppService.expireOutdatedOffers();
        } catch (Exception e) {
            // We swallow and log — otherwise the exception would kill the Spring Scheduler thread.
            System.err.println("[OfferExpirationCronJobAdapter] Expiring offers failed: "
                    + e.getMessage());
        }
    }
}
