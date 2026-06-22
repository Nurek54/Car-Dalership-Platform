package salon.sales.application.port.out;

import salon.common.model.Money;

/**
 * OUTBOUND PORT (Figure 22) — "BillingIntegration" to the Billing and Settlements Context.
 * Covers opening a settlement (UC-CRM-03), issuing a proforma on a declared bank transfer,
 * closing the balance on handover (UC-CRM-05) and settling a cancelled order.
 */
public interface BillingIntegration {

    /** UC-CRM-03: opens the settlement (contract value) when an order is placed. */
    void openSettlement(String orderId, Money contractValue);

    /** UC-CRM-03: requests a proforma invoice for the declared bank-transfer amount. */
    void requestProformaInvoice(String orderId, Money amount);

    /** UC-CRM-05: closes the order's balance and triggers the final VAT invoice. */
    void closeOrderBalance(String orderId);

    /** UC-CRM-03 (A1): settles/voids the account for a cancelled order, with the reason. */
    void processCancelledOrderBilling(String orderId, String reason);
}
