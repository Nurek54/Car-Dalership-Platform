package salon.sales.infrastructure.out.integration;

import org.springframework.stereotype.Component;
import salon.common.model.Money;
import salon.sales.application.port.out.BillingIntegration;

/**
 * OUTBOUND ADAPTER (ACL, Figure 22) — log-only integration with the Billing and Settlement Context
 * (used by the POJO/demo wiring). In a distributed setup this maps onto Billing command messages /
 * REST endpoints; here each operation is simulated by logging.
 */
@Component
public class BillingIntegrationAdapter implements BillingIntegration {

    @Override
    public void openSettlement(String orderId, Money contractValue) {
        System.out.println("[BillingIntegrationAdapter] Settlement opened in Billing for order "
                + orderId + ", contract value " + contractValue.getAmount() + " " + contractValue.currency() + ".");
    }

    @Override
    public void requestProformaInvoice(String orderId, Money amount) {
        System.out.println("[BillingIntegrationAdapter] Proforma invoice requested for order "
                + orderId + (amount != null ? ", amount " + amount.getAmount() + " " + amount.currency() : "") + ".");
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
