package salon.financing.application.port.out;

import salon.common.model.Money;
import salon.financing.application.domain.model.financing.BuyerDetails;

public interface SalesIntegration {

    BuyerDetails buyerDetails(String orderId);

    Money offerFinalPrice(String orderId);
}
