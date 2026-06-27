package salon.sales.infrastructure.out.integration;

import org.springframework.stereotype.Component;
import salon.common.model.Money;
import salon.sales.application.port.out.BillingIntegration;

@Component
public class BillingIntegrationAdapter implements BillingIntegration {

    @Override
    public void openSettlement(String orderId, Money contractValue) {
        System.out.println("[BillingIntegrationAdapter] Settlement opened in Billing for order "
                + orderId + ", contract value " + contractValue.amount() + " " + contractValue.currency() + ".");
    }

    @Override
    public void requestProformaInvoice(String orderId, Money amount) {
        System.out.println("[BillingIntegrationAdapter] Proforma invoice requested for order "
                + orderId + (amount != null ? ", amount " + amount.amount() + " " + amount.currency() : "") + ".");
    }

    @Override
    public void closeOrderBalance(String orderId) {
        System.out.println("[BillingIntegrationAdapter] Balance closed in Billing for order " + orderId + ".");
    }

    @Override
    public void processCancelledOrderBilling(String orderId, String reason) {
        System.out.println("[BillingIntegrationAdapter] Cancellation billing for order " + orderId
                + " (reason: " + reason + ").");
    }
}
