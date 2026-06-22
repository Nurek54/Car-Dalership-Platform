package salon.sales.application.port.in;

/**
 * INBOUND PORT (Figure 22) — "ExpireOutdatedOffer".
 * Periodically (CronJob) rejects published offers whose validity date has passed.
 */
public interface ExpireOutdatedOffer {

    void expireOutdatedOffers();
}
