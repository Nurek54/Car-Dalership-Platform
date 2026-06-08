package integration.driving_adapetrs.cron_job_adapter.sales;

import salon.sales.application.service.OfferAppService;

/**
 * Adapter sterujący (driving) — zadanie cykliczne unieważniania ofert po terminie (UC-SPR-01 A2).
 *
 * W kodzie produkcyjnym metoda ma nad sobą np. @Scheduled(cron = "0 0 1 * * ?").
 * Adapter jest cienki: deleguje do warstwy aplikacji i sam łapie wyjątki (ochrona wątku Schedulera).
 */
public class ExpiredOffersCronJobAdapter {

    private final OfferAppService offerAppService;

    public ExpiredOffersCronJobAdapter(OfferAppService offerAppService) {
        if (offerAppService == null) {
            throw new IllegalArgumentException("offerAppService must not be null.");
        }
        this.offerAppService = offerAppService;
    }

    // Wyzwalane przez harmonogram: posprzątaj oferty, którym minęła ważność.
    public void invalidateExpiredOffersJob() {
        try {
            offerAppService.processExpiredOffers();
        } catch (Exception e) {
            // Połykamy i logujemy — błąd zadania w tle nie może ubić wątku Spring Schedulera.
            System.err.println("[ExpiredOffersCronJobAdapter] Unieważnianie ofert nie powiodło się: "
                    + e.getMessage());
        }
    }
}
