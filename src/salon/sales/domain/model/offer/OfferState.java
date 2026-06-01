package salon.sales.domain.model.offer;

public enum OfferState {
    DRAFT,                      // robocza
    PENDING_DIRECTOR_APPROVAL,  // rabat przekroczył limit Handlowca (UC-SPR-01, A1)
    PUBLISHED,                  // wysłana klientowi
    EXPIRED,                    // ważność minęła
    CONVERTED                   // zamieniona na zamówienie
}
