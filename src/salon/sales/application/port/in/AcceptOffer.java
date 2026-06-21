package salon.sales.application.port.in;

import salon.sales.application.domain.model.offer.OfferId;

/**
 * Inbound port for UC-CRM-03 (offer acceptance and order creation) —
 * the "AcceptOffer/CreateOrderUseCase" node in docs/Architecture/SalesArchitecture.md
 * (PDF chapter 3.3.3 "Aggregate Transformation").
 */
public interface AcceptOffer {

    /** Main scenario: offer acceptance and order creation (returns its id). */
    String acceptOfferAndCreateOrder(OfferId offerId);
}
