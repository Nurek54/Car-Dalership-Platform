package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.BuyerDetails;

public interface SalesIntegration {

    BuyerDetails buyerDetailsFor(String orderId);
}
