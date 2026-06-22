package salon.sales.infrastructure.in.cron;

import salon.sales.application.port.in.ExpireOutdatedOffer;

/**
 * INBOUND ADAPTER (Figure 22: CronJob) — periodically drives the {@link ExpireOutdatedOffer} port,
 * rejecting published offers whose validity date has passed.
 */
public class OfferExpirationCronJob {

    private final ExpireOutdatedOffer expireOutdatedOffer;

    public OfferExpirationCronJob(ExpireOutdatedOffer expireOutdatedOffer) {
        if (expireOutdatedOffer == null) {
            throw new IllegalArgumentException("expireOutdatedOffer must not be null.");
        }
        this.expireOutdatedOffer = expireOutdatedOffer;
    }

    /** Scheduled entry point (e.g. invoked daily). */
    public void run() {
        this.expireOutdatedOffer.expireOutdatedOffers();
    }
}
