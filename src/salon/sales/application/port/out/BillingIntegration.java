package salon.sales.application.port.out;

import salon.common.model.Money;

/**
 * Outbound port (driven) to the Billing and Settlement Context.
 * Communication consistent with the canvases: request for a proforma/deposit, settlement of a cancelled
 * order and closing the balance after the vehicle handover.
 */
public interface BillingIntegration {

    /** UC-FIR-01/02: instruction to issue a proforma document for the declared amount. */
    void requestProformaInvoice(String orderId, Money amount);

    /** Settling the deposit after order cancellation (with the cancellation reason). */
    void processCancelledOrderBilling(String orderId, String reason);

    /** UC-CRM-05: closing the final balance after the physical vehicle handover. */
    void closeOrderBalance(String orderId);
}
