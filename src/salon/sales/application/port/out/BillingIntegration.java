package salon.sales.application.port.out;

import salon.common.model.Money;

public interface BillingIntegration {

    void openSettlement(String orderId, Money contractValue);
}
