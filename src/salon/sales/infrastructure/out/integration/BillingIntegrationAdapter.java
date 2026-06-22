package salon.sales.infrastructure.out.integration;

import salon.common.model.Money;
import salon.sales.application.port.out.BillingIntegration;

public class BillingIntegrationAdapter implements BillingIntegration {

    @Override
    public void openSettlement(String orderId, Money contractValue) {
        System.out.println("[BillingIntegrationAdapter] Settlement opened in Billing for order "
                + orderId + ", contract value " + contractValue.getAmount() + " " + contractValue.currency() + ".");
    }
}
