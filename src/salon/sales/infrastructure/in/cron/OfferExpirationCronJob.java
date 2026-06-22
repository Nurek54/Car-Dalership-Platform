package salon.sales.infrastructure.in.cron;

import salon.sales.application.port.in.ExpireOutdatedOffer;

public class OfferExpirationCronJob {

    private final ExpireOutdatedOffer expireOutdatedOffer;

    public OfferExpirationCronJob(ExpireOutdatedOffer expireOutdatedOffer) {
        if (expireOutdatedOffer == null) {
            throw new IllegalArgumentException("expireOutdatedOffer must not be null.");
        }
        this.expireOutdatedOffer = expireOutdatedOffer;
    }

    public void run() {
        this.expireOutdatedOffer.expireOutdatedOffers();
    }
}
