package salon.sales.infrastructure.in.cron;

import org.springframework.stereotype.Component;
import salon.sales.application.service.SalesService;

/**
 * Adapter sterujący (driving) — zadanie cykliczne wygaszania przeterminowanych ofert.
 *
 * W kodzie produkcyjnym metoda ma nad sobą np. @Scheduled(cron = "0 0 3 * * ?") (nocą).
 * Adapter deleguje do warstwy aplikacji i sam łapie wyjątki, żeby awaria pojedynczego
 * uruchomienia nie zatrzymała całego Spring Schedulera (cron odpali się ponownie).
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

    // Wyzwalane przez harmonogram: przeterminowane oferty wypadają z aktywnego obiegu.
    public void expireOldOffersJob() {
        try {
            salesAppService.expireOutdatedOffers();
        } catch (Exception e) {
            // Połykamy i logujemy — inaczej wyjątek ubije wątek Spring Schedulera.
            System.err.println("[OfferExpirationCronJobAdapter] Wygaszanie ofert nie powiodło się: "
                    + e.getMessage());
        }
    }
}
