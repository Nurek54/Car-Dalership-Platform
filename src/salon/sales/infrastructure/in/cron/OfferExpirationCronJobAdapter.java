package salon.sales.infrastructure.in.cron;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import salon.sales.application.service.SalesService;

@Component
public class OfferExpirationCronJobAdapter {

    private final SalesService salesAppService;

    public OfferExpirationCronJobAdapter(SalesService salesAppService) {
        this.salesAppService = salesAppService;
    }

    
    @Scheduled(cron = "0 0 2 * * *")
    public void expireOldOffersJob() {
        try {
            this.salesAppService.expireOutdatedOffers();
        } catch (RuntimeException ex) {
            
            System.err.println("[OfferExpirationCronJobAdapter] Offer expiration job failed: " + ex.getMessage());
        }
    }
}
