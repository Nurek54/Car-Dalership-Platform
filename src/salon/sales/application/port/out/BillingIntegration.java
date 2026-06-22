package salon.sales.application.port.out;

import salon.common.model.Money;

/**
 * OUTBOUND PORT (Figure 22) — "BillingIntegration".
 * UC-CRM-03: when an order is placed, opens the settlement (contract value) in the Billing and
 * Settlements Context. The adapter (BillingExternalAPI) handles the technical call.
 */
public interface BillingIntegration {

    void openSettlement(String orderId, Money contractValue);
}
