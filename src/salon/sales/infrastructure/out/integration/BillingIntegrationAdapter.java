package salon.sales.infrastructure.out.integration;

import salon.common.model.Money;
import salon.sales.application.port.out.BillingIntegration;

/**
 * OUTBOUND ADAPTER (ACL, Figure 22) — integration with the Billing and Settlement Context.
 *
 * Requests opening a settlement for an accepted order (UC-CRM-03); here simulated by logging.
 * In a distributed setup this maps onto a Billing command message / REST endpoint.
 */
public class BillingIntegrationAdapter implements BillingIntegration {

    @Override
    public void openSettlement(String orderId, Money contractValue) {
        System.out.println("[BillingIntegrationAdapter] Settlement opened in Billing for order "
                + orderId + ", contract value " + contractValue.getAmount() + " " + contractValue.currency() + ".");
    }
}
