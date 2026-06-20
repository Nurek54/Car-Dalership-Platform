package salon.sales.application.port.in;

import salon.sales.application.domain.model.offer.OfferId;

/**
 * Port wejściowy dla UC-CRM-03 (zatwierdzenie oferty i utworzenie zamówienia) —
 * węzeł "AcceptOffer/CreateOrderUseCase" w docs/Architecture/SalesArchitecture.md
 * (PDF rozdz. 3.3.3 "Transformacja Agregatów").
 */
public interface AcceptOffer {

    /** Scenariusz główny: akceptacja oferty i utworzenie zamówienia (zwraca jego id). */
    String acceptOfferAndCreateOrder(OfferId offerId);
}
